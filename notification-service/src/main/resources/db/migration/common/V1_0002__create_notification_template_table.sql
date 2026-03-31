CREATE SEQUENCE IF NOT EXISTS notification_schema.notification_template_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS notification_schema.notification_template
(
    id                 BIGINT       NOT NULL DEFAULT nextval('notification_schema.notification_template_seq'),
    channel            VARCHAR(50)  NOT NULL,
    code               VARCHAR(255) NOT NULL,
    name               VARCHAR(255),
    subject            VARCHAR(255),
    body               TEXT,
    description        VARCHAR(500),
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    CONSTRAINT pk_notification_template PRIMARY KEY (id),
    CONSTRAINT uq_notification_template_code_channel UNIQUE (code, channel)
);

CREATE INDEX IF NOT EXISTS idx_notification_template_code_channel
    ON notification_schema.notification_template (code, channel);

CREATE INDEX IF NOT EXISTS idx_notification_template_active
    ON notification_schema.notification_template (active);
