package com.humanize.ingestion;

public record ExtractionResult(String text, String source) {
    public int extractedChars() {
        return text == null ? 0 : text.length();
    }
}
