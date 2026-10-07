CREATE TABLE IF NOT EXISTS user_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    last_interaction_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_event UNIQUE (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_user_interactions_user
    ON user_interactions (user_id);

CREATE INDEX IF NOT EXISTS idx_user_interactions_event
    ON user_interactions (event_id);

CREATE INDEX IF NOT EXISTS idx_user_interactions_recent
    ON user_interactions (user_id, last_interaction_at DESC);


CREATE TABLE IF NOT EXISTS event_similarities (
    id BIGSERIAL PRIMARY KEY,
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_event_similarity UNIQUE (event_a, event_b),
    CONSTRAINT chk_event_pair_order CHECK (event_a < event_b)
);

CREATE INDEX IF NOT EXISTS idx_similarity_event_a
    ON event_similarities (event_a);

CREATE INDEX IF NOT EXISTS idx_similarity_event_b
    ON event_similarities (event_b);

CREATE INDEX IF NOT EXISTS idx_similarity_score
    ON event_similarities (score DESC);
