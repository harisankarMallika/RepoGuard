package com.repoguard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class SecretScannerTest {
    @TempDir Path dir;
    @Test void detectsCriticalAndHighCredentials() throws Exception {
        Files.writeString(dir.resolve("config.env"), "AWS_ACCESS_KEY_ID=AKIA1234567890" + "ABCDEF\npass" + "word = 'super-secret-value-123'\n");
        ScanReport report = new SecretScanner().scan(dir);
        assertEquals(2, report.findings().size());
        assertEquals(1, report.criticalCount()); assertEquals(1, report.highCount()); assertFalse(report.isClean());
    }
    @Test void ignoresBuildOutputAndAllowsCleanRepository() throws Exception {
        Files.createDirectories(dir.resolve("target")); Files.writeString(dir.resolve("target/leak.txt"), "ghp_abcdefghij" + "klmnopqrstuvwxyz1234567890");
        Files.writeString(dir.resolve("README.md"), "No credentials here.\n");
        ScanReport report = new SecretScanner().scan(dir);
        assertTrue(report.isClean()); assertEquals(1, report.filesScanned());
    }
    @Test void reportsPrivateKeyMaterial() throws Exception {
        Files.writeString(dir.resolve("key.txt"), "-----BEGIN " + "PRIVATE KEY-----\n");
        assertEquals(Finding.Severity.CRITICAL, new SecretScanner().scan(dir).findings().get(0).severity());
    }
}


