CREATE TABLE IF NOT EXISTS reviews (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    professional_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating          INTEGER      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (client_id, professional_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_professional_id ON reviews(professional_id);
