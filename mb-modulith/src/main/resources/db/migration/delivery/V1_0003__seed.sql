INSERT INTO delivery.person (id, name, email, registration_number, type)
SELECT 1, 'Mike', 'abc@gmail.com', '123456', 'CUSTOMER'
WHERE NOT EXISTS (SELECT 1 FROM delivery.person WHERE id = 1);

SELECT setval(pg_get_serial_sequence('delivery.person', 'id'), (SELECT COALESCE(MAX(id), 1) FROM delivery.person));
