-- Supplemental logging needs to be enabled to capture the required metadata for the CDC.
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;

-- Suppose the tables in the Oracle database contain a primary key.
-- In that case, minimal supplemental logging for the primary key needs to be enabled to ensure that these keys are captured during changes.
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;

-- Force logging ensures all the changes are recorded in the redo logs, even those that normally don’t generate redo logs like direct path inserts.
ALTER DATABASE FORCE LOGGING;

-- We need to set the value of the database parameter UNDO_RETENTION to allow Debezium enough time to mine the necessary data from the redo logs.
ALTER SYSTEM SET UNDO_RETENTION = 900;

-- Switch to the pluggable database
ALTER SESSION SET CONTAINER = FREEPDB1;

-- Setting Oracle to the archive log mode ensures that the redo logs are retained.
-- If not in ARCHIVELOG mode, let us run the query below to switch to it.
-- Note: ARCHIVELOG mode should be enabled at database creation time in containerized environments
-- The following commands cannot be executed in a running container context:
-- SHUTDOWN IMMEDIATE;
-- STARTUP MOUNT;
-- ALTER DATABASE ARCHIVELOG;
-- ALTER DATABASE OPEN;

-- Drop the user if it already exists (optional for repeatability)
BEGIN
    EXECUTE IMMEDIATE 'DROP USER MB_ORACLE_USER CASCADE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -01918 THEN -- user does not exist
            RAISE;
        END IF;
END;
/

-- Create a new user (schema)
CREATE USER MB_ORACLE_USER IDENTIFIED BY oracle_password;
GRANT DBA, CONNECT, RESOURCE TO MB_ORACLE_USER;

GRANT EXECUTE ON DBMS_LOGMNR TO MB_ORACLE_USER;
GRANT SELECT ON V_$LOGMNR_CONTENTS TO MB_ORACLE_USER;
GRANT SELECT ON V_$ARCHIVED_LOG TO MB_ORACLE_USER;
GRANT SELECT ON V_$LOG TO MB_ORACLE_USER;
GRANT SELECT ON V_$LOGFILE TO MB_ORACLE_USER;
GRANT SELECT ON V_$LOG_HISTORY TO MB_ORACLE_USER;
GRANT SELECT ON V_$DATABASE TO MB_ORACLE_USER;
GRANT SELECT ON V_$THREAD TO MB_ORACLE_USER;
GRANT SELECT ON V_$PARAMETER TO MB_ORACLE_USER;
GRANT SELECT ON V_$NLS_PARAMETERS TO MB_ORACLE_USER;
GRANT LOGMINING TO MB_ORACLE_USER;

-- Set current schema for table creation
ALTER SESSION SET CURRENT_SCHEMA = MB_ORACLE_USER;
