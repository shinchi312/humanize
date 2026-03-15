package com.humanize.contracts.payload;

public record BookUploadedPayload(
        String bookId,
        String userId,
        String objectKey,
        String contentType
) {
}
