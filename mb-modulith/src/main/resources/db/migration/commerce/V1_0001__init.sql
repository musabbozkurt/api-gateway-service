CREATE SCHEMA IF NOT EXISTS commerce;

CREATE SEQUENCE IF NOT EXISTS commerce.hibernate_sequence START WITH 1;

CREATE TABLE IF NOT EXISTS commerce.client
(
    id                uuid         not null primary key,
    created_date_time date         NOT NULL,
    deleted           boolean      NOT NULL default false,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    telephone         VARCHAR(255) NOT NULL,
    username          VARCHAR(255) NOT NULL unique,
    password          VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL unique
);

CREATE TABLE IF NOT EXISTS commerce.order
(
    id                  uuid         not null primary key,
    created_date_time   date         NOT NULL,
    deleted             boolean      NOT NULL default false,
    locked              boolean      NOT NULL default false,
    number              VARCHAR(255) NULL,
    delivery_address_id uuid         not null,
    pilotes             INT          NULL,
    order_total         DECIMAL      NULL,
    client_id           uuid         not null
);

CREATE TABLE IF NOT EXISTS commerce.address
(
    id                uuid         not null primary key,
    created_date_time date         NOT NULL,
    deleted           boolean      NOT NULL default false,
    street            VARCHAR(255) NULL,
    postcode          VARCHAR(255) NULL,
    city              VARCHAR(255) NULL,
    country           VARCHAR(255) NULL,
    client_id         uuid         not null
);

CREATE TABLE IF NOT EXISTS commerce.roles
(
    id                UUID        NOT NULL primary key,
    name              VARCHAR(20) NULL,
    created_date_time date        NOT NULL,
    deleted           boolean     NOT NULL default false
);

CREATE TABLE IF NOT EXISTS commerce.user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL
);

ALTER TABLE commerce.address
    ADD CONSTRAINT FK_ADDRESS_ON_ORDER FOREIGN KEY (client_id) REFERENCES commerce.client (id);

ALTER TABLE commerce.order
    ADD CONSTRAINT FK_ORDER_ON_DELIVERYADDRESS FOREIGN KEY (delivery_address_id) REFERENCES commerce.address (id);

ALTER TABLE commerce.order
    ADD CONSTRAINT FK_ORDER_ON_CLIENT FOREIGN KEY (client_id) REFERENCES commerce.client (id);
