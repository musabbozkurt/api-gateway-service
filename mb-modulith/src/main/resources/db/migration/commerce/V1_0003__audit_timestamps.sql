-- Align commerce audit columns with JPA (java.util.Date -> timestamp).
ALTER TABLE commerce.client
    ALTER COLUMN created_date_time TYPE timestamp USING created_date_time::timestamp;

ALTER TABLE commerce."order"
    ALTER COLUMN created_date_time TYPE timestamp USING created_date_time::timestamp;

ALTER TABLE commerce.address
    ALTER COLUMN created_date_time TYPE timestamp USING created_date_time::timestamp;

ALTER TABLE commerce.roles
    ALTER COLUMN created_date_time TYPE timestamp USING created_date_time::timestamp;
