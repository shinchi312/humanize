package com.humanize.ingestion;

import java.time.Instant;

public record BookProcessingState(
        String bookId,
        String status,
        String message,
        String source,
        int extractedChars,
        String title,
        String author,
        String genre,
        Instant updatedAt
) {
    public static BookProcessingState started(String bookId, String message) {
        return new BookProcessingState(bookId, "STARTED", message, "N/A", 0, "", "", "", Instant.now());
    }

    public static BookProcessingState failed(String bookId, String message) {
        return new BookProcessingState(bookId, "FAILED", message, "N/A", 0, "", "", "", Instant.now());
    }
}
