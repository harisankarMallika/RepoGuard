package com.repoguard;

import javax.swing.*;
import java.nio.file.Path;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args.length != 2 || !(args[0].equals("--scan") || args[0].equals("scan"))) {
                System.err.println("Usage: java -jar repoguard-1.0.0.jar --scan <repository-path>"); System.exit(2);
            }
            try {
                ScanReport report = new SecretScanner().scan(Path.of(args[1]));
                System.out.printf("RepoGuard scanned %d files in %d ms%n", report.filesScanned(), report.durationMillis());
                for (Finding finding : report.findings()) System.out.printf("%s %s:%d [%s] %s%n", finding.severity(), report.root().relativize(finding.file()), finding.line(), finding.rule(), finding.message());
                if (report.isClean()) System.out.println("PASS: no findings"); else { System.out.printf("FAIL: %d finding(s)%n", report.findings().size()); System.exit(1); }
            } catch (Exception e) { System.err.println("RepoGuard error: " + e.getMessage()); System.exit(2); }
        } else SwingUtilities.invokeLater(() -> new RepoGuardFrame().setVisible(true));
    }
}
