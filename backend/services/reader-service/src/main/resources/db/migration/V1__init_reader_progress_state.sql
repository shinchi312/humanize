CREATE TABLE IF NOT EXISTS reader_progress_state (
    progress_key VARCHAR(260) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    page INTEGER NOT NULL,
    progress_percent DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reader_progress_user_updated
    ON reader_progress_state (user_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_reader_progress_book_updated
    ON reader_progress_state (book_id, updated_at);
