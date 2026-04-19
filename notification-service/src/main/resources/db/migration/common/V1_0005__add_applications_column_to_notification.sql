ALTER TABLE notification_schema.notification
    ADD COLUMN IF NOT EXISTS applications TEXT;
