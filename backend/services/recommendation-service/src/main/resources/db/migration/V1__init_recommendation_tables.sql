CREATE TABLE IF NOT EXISTS reco_book_features (
    book_id VARCHAR(128) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(128) NOT NULL,
    source VARCHAR(64) NOT NULL,
    extracted_chars INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS reco_user_book_signals (
    signal_key VARCHAR(300) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    last_progress_percent DOUBLE PRECISION NOT NULL,
    last_page INTEGER NOT NULL,
    activity_events INTEGER NOT NULL,
    last_activity_type VARCHAR(64) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS reco_scores (
    score_key VARCHAR(300) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reco_book_features_genre
    ON reco_book_features (genre);

CREATE INDEX IF NOT EXISTS idx_reco_user_signals_user
    ON reco_user_book_signals (user_id);

CREATE INDEX IF NOT EXISTS idx_reco_user_signals_book
    ON reco_user_book_signals (book_id);

CREATE INDEX IF NOT EXISTS idx_reco_scores_user_score
    ON reco_scores (user_id, score);

CREATE INDEX IF NOT EXISTS idx_reco_scores_book
    ON reco_scores (book_id);
