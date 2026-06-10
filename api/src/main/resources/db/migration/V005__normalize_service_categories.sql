UPDATE services SET category = 'MASON'       WHERE category = 'Pedreiro';
UPDATE services SET category = 'ELECTRICIAN' WHERE category = 'Eletricista';
UPDATE services SET category = 'PLUMBER'     WHERE category = 'Encanador';
UPDATE services SET category = 'PAINTER'     WHERE category = 'Pintor';
UPDATE services SET category = 'HOUSEKEEPER' WHERE category = 'Diarista';
UPDATE services SET category = 'GARDENER'    WHERE category = 'Jardineiro';
UPDATE services SET category = 'CARPENTER'   WHERE category = 'Marceneiro';
UPDATE services SET category = 'MECHANIC'    WHERE category = 'Mecânico';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_services_category'
    ) THEN
        ALTER TABLE services
            ADD CONSTRAINT chk_services_category
            CHECK (category IN (
                'MASON', 'ELECTRICIAN', 'PLUMBER', 'PAINTER',
                'HOUSEKEEPER', 'GARDENER', 'CARPENTER', 'MECHANIC'
            ));
    END IF;
END $$;
