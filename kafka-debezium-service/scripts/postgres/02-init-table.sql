CREATE TABLE IF NOT EXISTS inventory.customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inventory.products
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    weight      INT
);

-- Set REPLICA IDENTITY to FULL to log all columns for UPDATE and DELETE operations
ALTER TABLE inventory.customers
    REPLICA IDENTITY FULL;

ALTER TABLE inventory.products
    REPLICA IDENTITY FULL;

-- Insert sample data
INSERT INTO inventory.customers (first_name, last_name, email)
VALUES ('Sally', 'Thomas', 'sally.thomas@acme.com'),
       ('George', 'Bailey', 'gbailey@foobar.com'),
       ('Edward', 'Walker', 'ed@walker.com'),
       ('Anne', 'Kretchmar', 'annek@noanswer.org');

INSERT INTO inventory.products (name, description, weight)
VALUES ('scooter', 'Small 2-wheel scooter', 3),
       ('car battery', '12V car battery', 8),
       ('12-pack drill bits', '12-pack of drill bits with sizes ranging from #40 to #3', 1),
       ('hammer', '12oz carpenter hammer', 2);

UPDATE inventory.customers
SET email = 'sally.thomas.updated@acme.commmm'
WHERE first_name = 'Sally';

UPDATE inventory.products
SET weight = 10
WHERE name = 'car battery';

DELETE
FROM inventory.customers
WHERE first_name = 'George';

DELETE
FROM inventory.products
WHERE name = 'hammer';
