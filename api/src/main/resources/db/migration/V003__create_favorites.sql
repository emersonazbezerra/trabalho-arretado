CREATE TABLE IF NOT EXISTS favorites (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    professional_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (client_id, professional_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_client_id ON favorites(client_id);
CREATE INDEX IF NOT EXISTS idx_favorites_professional_id ON favorites(professional_id);
