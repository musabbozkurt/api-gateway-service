CREATE SCHEMA IF NOT EXISTS config_server_schema;

CREATE TABLE IF NOT EXISTS config_server_schema.properties
(
    ID          INTEGER NOT NULL auto_increment,
    CREATED_ON  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    APPLICATION varchar(255),
    PROFILE     varchar(255),
    LABEL       varchar(255),
    PROP_KEY    varchar(255),
    PROP_VALUE  varchar(255),
    primary key (ID)
);

INSERT INTO config_server_schema.properties (APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE)
VALUES ('swagger-application', 'development', 'main', 'service.first-url', 'https://client-first-url-dev.com'),
       ('swagger-application', 'development', 'main', 'service.second-url', 'https://client-second-url-dev.com'),
       ('swagger-application', 'production', 'main', 'service.first-url', 'https://client-first-url-prod.com'),
       ('swagger-application', 'development', 'main', 'service.first-url', 'https://second-client-dev.com'),
       ('swagger-application', 'production', 'main', 'service.first-url', 'https://second-client-prod.com');
