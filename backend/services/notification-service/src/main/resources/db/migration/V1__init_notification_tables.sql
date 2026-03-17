CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id VARCHAR(128) PRIMARY KEY,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_address VARCHAR(255),
    spoiler_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    spoiler_min_progress_percent DOUBLE PRECISION NOT NULL DEFAULT 60.0,
    preferred_channel VARCHAR(32) NOT NULL DEFAULT 'EMAIL',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification_delivery_log (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    status VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    content_preview VARCHAR(1200),
    provider_detail VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notification_log_user_book_status_created
    ON notification_delivery_log (user_id, book_id, status, created_at DESC);
