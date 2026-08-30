CREATE TABLE grammar_generation_requests (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(64) NOT NULL,
    level VARCHAR(16) NOT NULL,
    target_language VARCHAR(16) NOT NULL,
    rules JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_grammar_generation_requests_user_type_created
    ON grammar_generation_requests (user_id, type, created_at);
