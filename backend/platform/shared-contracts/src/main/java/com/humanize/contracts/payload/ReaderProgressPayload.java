package com.humanize.contracts.payload;

public record ReaderProgressPayload(
        String userId,
        String bookId,
        int page,
        double progressPercent
) {
}
