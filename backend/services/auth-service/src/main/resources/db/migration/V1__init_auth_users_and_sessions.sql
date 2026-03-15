CREATE TABLE IF NOT EXISTS auth_users (
    user_id VARCHAR(128) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    last_login_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS auth_sessions (
    session_id VARCHAR(300) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    refresh_token VARCHAR(4096) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auth_users_email
    ON auth_users (email);

CREATE INDEX IF NOT EXISTS idx_auth_users_last_login
    ON auth_users (last_login_at);

CREATE INDEX IF NOT EXISTS idx_auth_sessions_user
    ON auth_sessions (user_id);

CREATE INDEX IF NOT EXISTS idx_auth_sessions_updated
    ON auth_sessions (updated_at);
