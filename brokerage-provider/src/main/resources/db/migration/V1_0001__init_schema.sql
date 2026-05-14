create schema if not exists brokerage_provider;

CREATE SEQUENCE IF NOT EXISTS brokerage_provider.hibernate_sequence START WITH 1;
CREATE SEQUENCE IF NOT EXISTS brokerage_provider.default_sequence START WITH 1;

CREATE TABLE if not exists brokerage_provider.users
(
    id                 bigint                not null primary key,
    deleted            boolean default false not null,
    created_date_time  timestamp with time zone,
    modified_date_time timestamp with time zone,
    name               varchar(255)          not null,
    surname            varchar(255)          not null,
    username           varchar(50) unique    not null,
    phone_number       varchar(50) unique    not null,
    email              varchar(255) unique   not null
);

CREATE TABLE if not exists brokerage_provider.orders
(
    id                 bigint                not null primary key,
    deleted            boolean default false not null,
    created_date_time  timestamp with time zone,
    modified_date_time timestamp with time zone,
    status             varchar(255)          not null,
    type               varchar(255)          not null,
    product_code       varchar(255)          not null,
    quantity           bigint                not null,
    user_id            bigint                not null
);

CREATE TABLE if not exists brokerage_provider.stocks
(
    id                 bigint                not null primary key,
    deleted            boolean default false not null,
    created_date_time  timestamp with time zone,
    modified_date_time timestamp with time zone,
    product_code       varchar(255) unique   not null,
    quantity           bigint                not null
);

insert into brokerage_provider.users (id, created_date_time, modified_date_time, name, surname, username, phone_number,
                                      email)
select (select nextval('brokerage_provider.default_sequence'::regclass)),
       now(),
       now(),
       'Emily',
       'Blunt',
       'emily_blunt',
       '1234567890',
       'emily.blunt@gmail.com'
where not exists (select 1
                  from brokerage_provider.users
                  where username = 'emily_blunt');

insert into brokerage_provider.users (id, created_date_time, modified_date_time, name, surname, username, phone_number,
                                      email)
select (select nextval('brokerage_provider.default_sequence'::regclass)),
       now(),
       now(),
       'Emma',
       'Stone',
       'emma_stone',
       '1234567891',
       'emma.stone@gmail.com'
where not exists (select 1
                  from brokerage_provider.users
                  where username = 'emma_stone');

insert into brokerage_provider.users (id, created_date_time, modified_date_time, name, surname, username, phone_number,
                                      email)
select (select nextval('brokerage_provider.default_sequence'::regclass)),
       now(),
       now(),
       'Peter',
       'Jackson',
       'peter_jackson',
       '1234567892',
       'peter.jackson@gmail.com'
where not exists (select 1
                  from brokerage_provider.users
                  where username = 'peter_jackson');

insert into brokerage_provider.users (id, created_date_time, modified_date_time, name, surname, username, phone_number,
                                      email)
select (select nextval('brokerage_provider.default_sequence'::regclass)),
       now(),
       now(),
       'Chris',
       'Rock',
       'chris_rock',
       '1234567893',
       'chris.rock@gmail.com'
where not exists (select 1
                  from brokerage_provider.users
                  where username = 'chris_rock');

insert into brokerage_provider.stocks (id, created_date_time, modified_date_time, product_code, quantity)
select (select nextval('brokerage_provider.default_sequence'::regclass)), now(), now(), 'APPLE', 10
where not exists (select 1
                  from brokerage_provider.stocks
                  where product_code = 'APPLE');

insert into brokerage_provider.stocks (id, created_date_time, modified_date_time, product_code, quantity)
select (select nextval('brokerage_provider.default_sequence'::regclass)), now(), now(), 'TESLA', 20
where not exists (select 1
                  from brokerage_provider.stocks
                  where product_code = 'TESLA');