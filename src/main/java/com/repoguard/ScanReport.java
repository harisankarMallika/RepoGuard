package com.repoguard;

import java.nio.file.Path;
import java.util.List;

public record ScanReport(Path root, int filesScanned, List<Finding> findings, long durationMillis) {
    public long criticalCount() { return findings.stream().filter(f -> f.severity() == Finding.Severity.CRITICAL).count(); }
    public long highCount() { return findings.stream().filter(f -> f.severity() == Finding.Severity.HIGH).count(); }
    public long mediumCount() { return findings.stream().filter(f -> f.severity() == Finding.Severity.MEDIUM).count(); }
    public boolean isClean() { return findings.isEmpty(); }
}
