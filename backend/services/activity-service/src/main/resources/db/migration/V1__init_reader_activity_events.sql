CREATE TABLE IF NOT EXISTS reader_activity_events (
    event_id VARCHAR(128) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    activity_type VARCHAR(64) NOT NULL,
    metadata_json VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_user_created
    ON reader_activity_events (user_id, created_at);

CREATE INDEX IF NOT EXISTS idx_activity_book_created
    ON reader_activity_events (book_id, created_at);

CREATE INDEX IF NOT EXISTS idx_activity_type
    ON reader_activity_events (activity_type);
