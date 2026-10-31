#!/bin/bash
set -euo pipefail

# Idempotent: safe if the data volume is re-used or this script is adjusted later.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-'EOSQL'
	DO $$
	BEGIN
	    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'mb') THEN
	        CREATE ROLE mb LOGIN PASSWORD 'mb';
	    END IF;
	END
	$$;
EOSQL

if ! psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -tAc \
    "SELECT 1 FROM pg_database WHERE datname = 'mb_modulith'" | grep -q 1; then
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    -c "CREATE DATABASE mb_modulith OWNER mb"
fi
