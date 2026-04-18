CREATE TABLE IF NOT EXISTS services (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    professional_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    estimated_price DECIMAL(10, 2),
    category        VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_services_professional_id ON services(professional_id);
CREATE INDEX IF NOT EXISTS idx_services_category ON services(category);
