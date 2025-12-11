CREATE SCHEMA IF NOT EXISTS config_server_schema;

CREATE TABLE IF NOT EXISTS config_server_schema.properties
(
    ID          INTEGER NOT NULL AUTO_INCREMENT,
    CREATED_ON  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    APPLICATION VARCHAR(255),
    PROFILE     VARCHAR(255),
    LABEL       VARCHAR(255),
    PROP_KEY    VARCHAR(255),
    PROP_VALUE  VARCHAR(255),
    PRIMARY KEY (ID)
);

INSERT INTO config_server_schema.properties (APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE)
VALUES ('swagger-application', 'development', 'main', 'service.first-url', 'https://client-first-url-dev.com'),
       ('swagger-application', 'development', 'main', 'service.second-url', 'https://client-second-url-dev.com'),
       ('swagger-application', 'production', 'main', 'service.first-url', 'https://client-first-url-prod.com'),
       ('swagger-application', 'development', 'main', 'service.first-url', 'https://second-client-dev.com'),
       ('swagger-application', 'production', 'main', 'service.first-url', 'https://second-client-prod.com');
