package com.repoguard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SecretScanner {
    private static final Set<String> IGNORED = Set.of(".git", ".idea", ".vscode", "target", "node_modules", "build", "dist", ".gradle");
    private static final long MAX_BYTES = 1_000_000;
    private record Rule(String id, Finding.Severity severity, Pattern pattern, String message) {}
    private static final List<Rule> RULES = List.of(
        new Rule("AWS_ACCESS_KEY", Finding.Severity.CRITICAL, Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"), "AWS access key identifier"),
        new Rule("PRIVATE_KEY", Finding.Severity.CRITICAL, Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"), "Private key material"),
        new Rule("GITHUB_TOKEN", Finding.Severity.CRITICAL, Pattern.compile("\\b(?:gh[pousr]_[A-Za-z0-9_]{20,}|github_pat_[A-Za-z0-9_]{20,})\\b"), "GitHub personal access token"),
        new Rule("GENERIC_SECRET", Finding.Severity.HIGH, Pattern.compile("(?i)(?:password|passwd|secret|api[_-]?key|access[_-]?token)\\s*[=:]\\s*[\\\"']?[^\\s\\\"']{8,}"), "Possible hard-coded credential"),
        new Rule("SLACK_TOKEN", Finding.Severity.HIGH, Pattern.compile("\\bxox[baprs]-[A-Za-z0-9-]{10,}\\b"), "Slack token"),
        new Rule("ENV_FILE", Finding.Severity.MEDIUM, Pattern.compile("(?i)^\\s*(?:AWS_SECRET_ACCESS_KEY|DATABASE_URL|PRIVATE_KEY)\\s*=\\s*[^#\\s]{6,}\\s*$"), "Credential-like environment assignment")
    );

    public ScanReport scan(Path input) throws IOException {
        Path root = input.toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) throw new IOException("Repository folder does not exist: " + root);
        long start = System.nanoTime();
        List<Finding> findings = new ArrayList<>();
        int[] scanned = {0};
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                if (isIgnored(root.relativize(file))) continue;
                long size;
                try { size = Files.size(file); } catch (IOException e) { continue; }
                if (size == 0 || size > MAX_BYTES || isBinary(file)) continue;
                scanned[0]++;
                scanFile(root, file, findings);
            }
        }
        findings.sort(Comparator.comparing((Finding f) -> f.severity().ordinal()).thenComparing(f -> root.relativize(f.file()).toString()).thenComparingInt(Finding::line));
        return new ScanReport(root, scanned[0], List.copyOf(findings), (System.nanoTime() - start) / 1_000_000);
    }

    private boolean isIgnored(Path relative) {
        for (Path part : relative) if (IGNORED.contains(part.toString())) return true;
        return false;
    }
    private boolean isBinary(Path file) {
        String n = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return n.endsWith(".jar") || n.endsWith(".class") || n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".pdf") || n.endsWith(".zip") || n.endsWith(".exe") || n.endsWith(".dll") || n.endsWith(".ico");
    }
    private void scanFile(Path root, Path file, List<Finding> out) {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; int number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                for (Rule rule : RULES) if (rule.pattern().matcher(line).find()) {
                    String preview = line.strip();
                    if (preview.length() > 140) preview = preview.substring(0, 137) + "...";
                    out.add(new Finding(file, number, rule.severity(), rule.id(), rule.message(), preview));
                }
            }
        } catch (IOException ignored) { /* unreadable/non-UTF8 files are skipped */ }
    }
}
