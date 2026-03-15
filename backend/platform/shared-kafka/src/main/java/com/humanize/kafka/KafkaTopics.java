package com.humanize.kafka;

import java.util.List;

public final class KafkaTopics {
    public static final String USER_LIFECYCLE = "user.lifecycle";
    public static final String BOOK_UPLOADED = "book.uploaded";
    public static final String BOOK_PROCESSING = "book.processing";
    public static final String READER_PROGRESS = "reader.progress";
    public static final String READER_ACTIVITY = "reader.activity";
    public static final String NOTIFICATION_LIFECYCLE = "notification.lifecycle";

    public static final List<String> ALL_TOPICS = List.of(
            USER_LIFECYCLE,
            BOOK_UPLOADED,
            BOOK_PROCESSING,
            READER_PROGRESS,
            READER_ACTIVITY,
            NOTIFICATION_LIFECYCLE
    );

    private KafkaTopics() {
    }
}
