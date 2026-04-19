CREATE SEQUENCE IF NOT EXISTS notification_schema.device_token_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS notification_schema.device_token
(
    id                 BIGINT       NOT NULL DEFAULT nextval('notification_schema.device_token_seq'),
    user_id            BIGINT       NOT NULL,
    token              VARCHAR(512) NOT NULL,
    platform           VARCHAR(20)  NOT NULL,
    application        VARCHAR(255) NOT NULL,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_device_token PRIMARY KEY (id),
    CONSTRAINT uq_device_token_user_app UNIQUE (user_id, application)
);
