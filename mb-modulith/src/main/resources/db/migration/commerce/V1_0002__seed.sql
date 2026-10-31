insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'ROLE_USER', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'ROLE_MODERATOR', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'ROLE_ADMIN', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'CREATE_CLIENT', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'UPDATE_CLIENT', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'GET_CLIENT', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'DELETE_CLIENT', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'CREATE_ORDER', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'UPDATE_ORDER', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'GET_ORDER', now(), false);

insert into commerce.roles (id, name, created_date_time, deleted)
values (gen_random_uuid(), 'DELETE_ORDER', now(), false);

insert into commerce.client (id, created_date_time, deleted, first_name, last_name, telephone, username, password,
                             email)
values (gen_random_uuid(), now(), false, 'MB', 'MUSEMENT', '1234567890', 'MBMUSEMENT',
        '$2a$10$31h28pxcjOw3dYFdd9sOwekZFHEgRHx1oQ8GWIGgd0T1uXTInq8Wq', 'mbmusement@gmail.com');

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'ROLE_USER'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'ROLE_MODERATOR'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'ROLE_ADMIN'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'CREATE_CLIENT'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'UPDATE_CLIENT'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'GET_CLIENT'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'DELETE_CLIENT'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'CREATE_ORDER'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'UPDATE_ORDER'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'GET_ORDER'));

insert into commerce.user_roles (user_id, role_id)
values ((select id from commerce.client where username = 'MBMUSEMENT'),
        (select id from commerce.roles where name = 'DELETE_ORDER'));
