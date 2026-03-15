package com.humanize.contracts.payload;

public record BookMetadataExtractedPayload(
        String bookId,
        String title,
        String author,
        String genre,
        String source,
        int extractedChars
) {
}
