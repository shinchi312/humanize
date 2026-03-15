package com.humanize.contracts.payload;

public record NotificationLifecyclePayload(
        String userId,
        String bookId,
        String status,
        String channel,
        String contentPreview
) {
}
