CREATE TABLE IF NOT EXISTS library_books (
    book_id VARCHAR(128) PRIMARY KEY,
    object_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    status VARCHAR(48) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS library_user_books (
    mapping_key VARCHAR(300) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    book_id VARCHAR(128) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    is_public BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_library_books_status
    ON library_books (status);

CREATE INDEX IF NOT EXISTS idx_library_books_updated
    ON library_books (updated_at);

CREATE INDEX IF NOT EXISTS idx_library_user_books_user
    ON library_user_books (user_id, updated_at);

CREATE INDEX IF NOT EXISTS idx_library_user_books_book
    ON library_user_books (book_id);
