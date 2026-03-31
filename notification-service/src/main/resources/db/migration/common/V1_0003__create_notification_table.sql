CREATE SEQUENCE IF NOT EXISTS notification_schema.notification_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS notification_schema.notification
(
    id                  BIGINT      NOT NULL DEFAULT nextval('notification_schema.notification_seq'),
    channel             VARCHAR(50),
    level               VARCHAR(50) NOT NULL DEFAULT 'INFO',
    type                VARCHAR(50),
    subject             VARCHAR(255),
    body                TEXT,
    title               VARCHAR(255),
    template_code       VARCHAR(255),
    template_parameters TEXT,
    data                TEXT,
    user_id             BIGINT,
    recipients          VARCHAR(1000),
    cc                  VARCHAR(1000),
    bcc                 VARCHAR(1000),
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message       VARCHAR(2000),
    retry_count         INTEGER     NOT NULL DEFAULT 0,
    is_read             BOOLEAN     NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    created_by          VARCHAR(255),
    created_date        TIMESTAMP,
    last_modified_by    VARCHAR(255),
    last_modified_date  TIMESTAMP,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id
    ON notification_schema.notification (user_id);

CREATE INDEX IF NOT EXISTS idx_notification_user_id_channel
    ON notification_schema.notification (user_id, channel);

CREATE INDEX IF NOT EXISTS idx_notification_user_id_is_read
    ON notification_schema.notification (user_id, is_read);

CREATE INDEX IF NOT EXISTS idx_notification_status
    ON notification_schema.notification (status);

CREATE INDEX IF NOT EXISTS idx_notification_channel
    ON notification_schema.notification (channel);

CREATE INDEX IF NOT EXISTS idx_notification_created_date
    ON notification_schema.notification (created_date);
