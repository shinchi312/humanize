package com.humanize.ingestion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResolvedPdfSource implements AutoCloseable {
    private final Path filePath;
    private final String sourceType;
    private final boolean deleteOnClose;

    public ResolvedPdfSource(Path filePath, String sourceType, boolean deleteOnClose) {
        this.filePath = filePath;
        this.sourceType = sourceType;
        this.deleteOnClose = deleteOnClose;
    }

    public Path filePath() {
        return filePath;
    }

    public String sourceType() {
        return sourceType;
    }

    @Override
    public void close() {
        if (!deleteOnClose) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup of temporary download file.
        }
    }
}
