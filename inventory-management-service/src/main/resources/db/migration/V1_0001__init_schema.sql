create schema if not exists inventory_management_service;

create sequence if not exists inventory_management_service.hibernate_sequence START WITH 1;
create sequence if not exists inventory_management_service.default_sequence START WITH 1;

create table if not exists inventory_management_service.categories
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

create table if not exists inventory_management_service.products
(
    id                 bigint                   not null primary key,
    deleted            boolean default false    not null,
    created_date_time  timestamp with time zone not null,
    modified_date_time timestamp with time zone,
    name               varchar(255) unique      not null,
    product_code       varchar(255) unique      not null,
    description        varchar(255) unique      not null,
    quantity           int                      not null,
    current_price      numeric(19, 2)           not null,
    currency           varchar(10)              not null,
    category_id        bigint references inventory_management_service.categories (id)
);

insert into inventory_management_service.categories (id, created_date_time, modified_date_time, name, description,
                                                     live_in_market, version)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'Electronics Category',
       'Electronics Category Description',
       false,
       0
where not exists (select 1
                  from inventory_management_service.products
                  where name = 'Electronics Category');

insert into inventory_management_service.categories (id, created_date_time, modified_date_time, name, description,
                                                     live_in_market, version)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'Beauty Category',
       'Beauty Category Description',
       false,
       0
where not exists (select 1
                  from inventory_management_service.categories
                  where name = 'Beauty Category');

insert into inventory_management_service.products (id, created_date_time, modified_date_time, name, product_code,
                                                   description, quantity, current_price, currency, category_id)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'IPHONE 13',
       'IPHONE_13',
       'IPHONE 13 APPLE Product',
       10,
       2500,
       'EUR',
       (select c.id from inventory_management_service.categories c where c.name = 'Electronics Category')
where not exists (select 1
                  from inventory_management_service.products
                  where product_code = 'IPHONE_13');

insert into inventory_management_service.products (id, created_date_time, modified_date_time, name, product_code,
                                                   description, quantity, current_price, currency, category_id)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'IPHONE 14',
       'IPHONE_14',
       'IPHONE 14 APPLE Product',
       10,
       2700,
       'EUR',
       (select c.id from inventory_management_service.categories c where c.name = 'Electronics Category')
where not exists (select 1
                  from inventory_management_service.products
                  where product_code = 'IPHONE_14');

insert into inventory_management_service.products (id, created_date_time, modified_date_time, name, product_code,
                                                   description, quantity, current_price, currency)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'Novel',
       'Novel',
       'Novel Product Description',
       10,
       10.0,
       'EUR'
where not exists (select 1
                  from inventory_management_service.products
                  where product_code = 'Novel');

insert into inventory_management_service.products (id, created_date_time, modified_date_time, name, product_code,
                                                   description, quantity, current_price, currency)
select (select nextval('inventory_management_service.default_sequence'::regclass)),
       now(),
       now(),
       'Art',
       'Art',
       'Art Product Description',
       2000,
       6.0,
       'EUR'
where not exists (select 1
                  from inventory_management_service.products
                  where product_code = 'Art');
