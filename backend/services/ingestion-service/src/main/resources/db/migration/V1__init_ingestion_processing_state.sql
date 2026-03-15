CREATE TABLE IF NOT EXISTS ingestion_processing_state (
    book_id VARCHAR(128) PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    source VARCHAR(64) NOT NULL,
    extracted_chars INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ingestion_state_status_updated
    ON ingestion_processing_state (status, updated_at);

CREATE INDEX IF NOT EXISTS idx_ingestion_state_genre
    ON ingestion_processing_state (genre);
