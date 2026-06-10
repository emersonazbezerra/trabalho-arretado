ALTER TABLE services DROP CONSTRAINT IF EXISTS chk_services_category;

UPDATE services SET category = 'MASONRY'    WHERE category = 'MASON';
UPDATE services SET category = 'ELECTRICAL' WHERE category = 'ELECTRICIAN';
UPDATE services SET category = 'PLUMBING'   WHERE category = 'PLUMBER';
UPDATE services SET category = 'PAINTING'   WHERE category = 'PAINTER';
UPDATE services SET category = 'CLEANING'   WHERE category = 'HOUSEKEEPER';
UPDATE services SET category = 'GARDENING'  WHERE category = 'GARDENER';
UPDATE services SET category = 'CARPENTRY'  WHERE category = 'CARPENTER';
UPDATE services SET category = 'MECHANICS'  WHERE category = 'MECHANIC';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_services_category'
    ) THEN
        ALTER TABLE services
            ADD CONSTRAINT chk_services_category
            CHECK (category IN (
                'MASONRY', 'ELECTRICAL', 'PLUMBING', 'PAINTING',
                'CLEANING', 'GARDENING', 'CARPENTRY', 'MECHANICS'
            ));
    END IF;
END $$;
