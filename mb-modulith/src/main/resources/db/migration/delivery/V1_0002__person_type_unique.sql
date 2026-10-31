ALTER TABLE delivery.person
    ADD COLUMN IF NOT EXISTS type VARCHAR(255);

UPDATE delivery.person
SET type = 'CUSTOMER'
WHERE type IS NULL;

ALTER TABLE delivery.person
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE delivery.person
    ADD CONSTRAINT unique_person_email_name_type UNIQUE (email, name, type);
