-- Hibernate Envers (RevisionEntity) uses a pooled sequence with allocationSize 50.
-- The sequence was originally created with INCREMENT BY 1, which fails schema validation.
ALTER SEQUENCE email.revinfo_seq INCREMENT BY 50;
