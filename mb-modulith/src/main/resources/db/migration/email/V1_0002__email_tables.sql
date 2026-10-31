CREATE TABLE IF NOT EXISTS email.email
(
    id                uuid                     NOT NULL PRIMARY KEY,
    created_date_time timestamp with time zone NOT NULL,
    deleted           boolean                  NOT NULL DEFAULT false,
    email_address     varchar(255)             NOT NULL UNIQUE,
    email_count       bigint                   NOT NULL DEFAULT 0,
    blocked_domain    boolean                  NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS email.revinfo
(
    rev      integer NOT NULL PRIMARY KEY,
    revtstmp bigint
);

CREATE SEQUENCE IF NOT EXISTS email.revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS email.email_aud
(
    id                uuid    NOT NULL,
    rev               integer NOT NULL,
    revtype           smallint,
    email_address     varchar(255),
    email_address_mod boolean,
    email_count       bigint,
    blocked_domain    boolean,
    created_date_time timestamp with time zone,
    deleted           boolean,
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_email_aud_rev FOREIGN KEY (rev) REFERENCES email.revinfo (rev)
);
