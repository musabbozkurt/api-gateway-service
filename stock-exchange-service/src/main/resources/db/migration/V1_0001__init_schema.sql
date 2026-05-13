create schema if not exists stock_exchange_service;

CREATE SEQUENCE IF NOT EXISTS stock_exchange_service.hibernate_sequence START WITH 1;
CREATE SEQUENCE IF NOT EXISTS stock_exchange_service.default_sequence START WITH 1;

CREATE TABLE if not exists stock_exchange_service.users
(
    id                 bigint                   not null primary key,
    deleted            boolean default false    not null,
    enabled            boolean default true     not null,
    created_date_time  timestamp with time zone not null,
    modified_date_time timestamp with time zone,
    first_name         varchar(255)             not null,
    last_name          varchar(255)             not null,
    username           varchar(50) unique       not null,
    password           varchar(255)             not null,
    phone_number       varchar(50) unique       not null,
    email              varchar(255) unique      not null
);

CREATE TABLE IF NOT EXISTS stock_exchange_service.roles
(
    id                 bigint                   not null primary key,
    name               varchar(255)             not null,
    created_date_time  timestamp with time zone not null,
    modified_date_time timestamp with time zone,
    default_role       boolean                  not null default false,
    deleted            boolean                  not null default false
);

CREATE TABLE IF NOT EXISTS stock_exchange_service.authorities
(
    user_id   bigint not null,
    role_id   bigint not null,
    username  varchar(255),
    authority varchar(255),
    foreign key (user_id) references stock_exchange_service.users (id),
    foreign key (role_id) references stock_exchange_service.roles (id),
    primary key (user_id, role_id)
);

CREATE TABLE if not exists stock_exchange_service.stocks
(
    id                 bigint                   not null primary key,
    deleted            boolean default false    not null,
    created_date_time  timestamp with time zone not null,
    modified_date_time timestamp with time zone,
    name               varchar(255) unique      not null,
    description        varchar(255) unique      not null,
    current_price      numeric(19, 2)           not null
);

CREATE TABLE if not exists stock_exchange_service.stock_exchanges
(
    id                 bigint                   not null primary key,
    deleted            boolean                  not null default false,
    created_date_time  timestamp with time zone not null,
    modified_date_time timestamp with time zone,
    name               varchar(255) unique      not null,
    description        varchar(255) unique      not null,
    live_in_market     boolean                  not null default false,
    version            int
);

CREATE TABLE stock_exchange_service.stock_exchanges_stocks
(
    stock_exchange_id bigint references stock_exchange_service.stock_exchanges (id),
    stock_id          bigint references stock_exchange_service.stocks (id)
);

insert into stock_exchange_service.users (id, created_date_time, modified_date_time, first_name, last_name, username,
                                          password, phone_number, email)
select (select nextval('stock_exchange_service.default_sequence'::regclass)),
       now(),
       now(),
       'Admin',
       'User',
       'admin_user',
       '$2a$10$31h28pxcjOw3dYFdd9sOwekZFHEgRHx1oQ8GWIGgd0T1uXTInq8Wq',
       '1234567892',
       'admin.user@gmail.com'
where not exists (select 1
                  from stock_exchange_service.users
                  where username = 'admin_user');

insert into stock_exchange_service.stocks (id, created_date_time, modified_date_time, name, description, current_price)
select (select nextval('stock_exchange_service.default_sequence'::regclass)), now(), now(), 'AAPL', 'APPLE', 10
where not exists (select 1
                  from stock_exchange_service.stocks
                  where name = 'AAPL');

insert into stock_exchange_service.stocks (id, created_date_time, modified_date_time, name, description, current_price)
select (select nextval('stock_exchange_service.default_sequence'::regclass)), now(), now(), 'TSLA', 'TESLA', 20
where not exists (select 1
                  from stock_exchange_service.stocks
                  where name = 'TSLA');

insert into stock_exchange_service.stock_exchanges (id, created_date_time, modified_date_time, name, description,
                                                    live_in_market, version)
select (select nextval('stock_exchange_service.default_sequence'::regclass)),
       now(),
       now(),
       'New York Stock Exchange',
       'New York Stock Exchange Description',
       false,
       0
where not exists (select 1
                  from stock_exchange_service.stock_exchanges
                  where name = 'New York Stock Exchange');

insert into stock_exchange_service.stock_exchanges (id, created_date_time, modified_date_time, name, description,
                                                    live_in_market, version)
select (select nextval('stock_exchange_service.default_sequence'::regclass)),
       now(),
       now(),
       'London Stock Exchange',
       'London Stock Exchange Description',
       false,
       0
where not exists (select 1
                  from stock_exchange_service.stocks
                  where name = 'London Stock Exchange');

insert into stock_exchange_service.stock_exchanges_stocks (stock_exchange_id, stock_id)
select (select id from stock_exchange_service.stock_exchanges where name = 'New York Stock Exchange'),
       (select id from stock_exchange_service.stock_exchanges where name = 'AAPL')
where not exists (select 1
                  from stock_exchange_service.stock_exchanges_stocks
                  where stock_exchange_id =
                        (select id from stock_exchange_service.stock_exchanges where name = 'New York Stock Exchange')
                    and stock_id = (select id from stock_exchange_service.stock_exchanges where name = 'AAPL'));

insert into stock_exchange_service.stock_exchanges_stocks (stock_exchange_id, stock_id)
select (select id from stock_exchange_service.stock_exchanges where name = 'London Stock Exchange'),
       (select id from stock_exchange_service.stock_exchanges where name = 'TSLA')
where not exists (select 1
                  from stock_exchange_service.stock_exchanges_stocks
                  where stock_exchange_id =
                        (select id from stock_exchange_service.stock_exchanges where name = 'London Stock Exchange')
                    and stock_id = (select id from stock_exchange_service.stock_exchanges where name = 'TSLA'));

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'CREATE_STOCK', now(), true, false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'UPDATE_STOCK', now(), true, false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'GET_STOCK', now(), true, false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'DELETE_STOCK', now(), true, false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'CREATE_STOCK_EXCHANGE', now(), true,
        false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'ADD_STOCK', now(), true, false);

insert into stock_exchange_service.roles (id, name, created_date_time, default_role, deleted)
values ((select nextval('stock_exchange_service.default_sequence'::regclass)), 'REMOVE_STOCK', now(), true, false);

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'CREATE_STOCK'),
        'admin_user', 'CREATE_STOCK');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'UPDATE_STOCK'),
        'admin_user', 'UPDATE_STOCK');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'GET_STOCK'),
        'admin_user', 'GET_STOCK');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'DELETE_STOCK'),
        'admin_user', 'DELETE_STOCK');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'ADD_STOCK'),
        'admin_user', 'ADD_STOCK');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'CREATE_STOCK_EXCHANGE'),
        'admin_user', 'CREATE_STOCK_EXCHANGE');

insert into stock_exchange_service.authorities (user_id, role_id, username, authority)
values ((select id from stock_exchange_service.users where username = 'admin_user'),
        (select id from stock_exchange_service.roles where name = 'REMOVE_STOCK'),
        'admin_user', 'REMOVE_STOCK');
