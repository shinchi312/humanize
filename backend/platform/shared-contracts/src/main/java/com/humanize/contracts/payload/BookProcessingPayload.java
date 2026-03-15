package com.humanize.contracts.payload;

public record BookProcessingPayload(
        String bookId,
        String status,
        String message
) {
}
