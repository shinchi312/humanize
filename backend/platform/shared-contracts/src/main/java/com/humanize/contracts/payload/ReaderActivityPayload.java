package com.humanize.contracts.payload;

import java.util.Map;

public record ReaderActivityPayload(
        String userId,
        String bookId,
        String activityType,
        Map<String, String> metadata
) {
}
