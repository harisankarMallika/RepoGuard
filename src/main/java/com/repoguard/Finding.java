package com.repoguard;

import java.nio.file.Path;
import java.util.List;

public record Finding(Path file, int line, Severity severity, String rule, String message, String preview) {
    public enum Severity { CRITICAL, HIGH, MEDIUM }
}
