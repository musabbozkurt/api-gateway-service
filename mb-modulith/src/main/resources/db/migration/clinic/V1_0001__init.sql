create schema if not exists clinics;

CREATE TABLE if not exists clinics.doctors
(
    id           integer                NOT NULL primary key,
    name         character varying(255) NOT NULL,
    surname      character varying(255) NOT NULL,
    per_hour_fee numeric(19, 2)         not null,
    deleted      boolean default false  not null
);

CREATE SEQUENCE IF NOT EXISTS clinics.doctors_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE if not exists clinics.patients
(
    id      integer               NOT NULL primary key,
    name    character varying(255),
    surname character varying(255),
    deleted boolean default false not null
);

CREATE SEQUENCE if not exists clinics.patients_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE if not exists clinics.appointments
(
    id           integer                NOT NULL primary key,
    doctor_id    integer,
    end_date     timestamp without time zone,
    fee          numeric(19, 2)         not null,
    patient_id   integer,
    refunded_fee numeric(19, 2)         not null,
    start_date   timestamp without time zone,
    status       character varying(255) not null,
    deleted      boolean default false  not null
);

CREATE SEQUENCE if not exists clinics.appointments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY clinics.doctors
    ALTER COLUMN id SET DEFAULT nextval('clinics.doctors_id_seq'::regclass);

ALTER TABLE ONLY clinics.patients
    ALTER COLUMN id SET DEFAULT nextval('clinics.patients_id_seq'::regclass);

ALTER TABLE ONLY clinics.appointments
    ALTER COLUMN id SET DEFAULT nextval('clinics.appointments_id_seq'::regclass);

insert into clinics.doctors (id, name, surname, per_hour_fee)
values ((select nextval('clinics.doctors_id_seq'::regclass)), 'Emily', 'Blunt', 300.00),
       ((select nextval('clinics.doctors_id_seq'::regclass)), 'Emma', 'Stone', 300.00),
       ((select nextval('clinics.doctors_id_seq'::regclass)), 'Peter', 'Jackson', 300.00),
       ((select nextval('clinics.doctors_id_seq'::regclass)), 'Chris', 'Rock', 300.00);

insert into clinics.patients (id, name, surname)
values ((select nextval('clinics.doctors_id_seq'::regclass)), 'Mikey', 'Pack'),
       ((select nextval('clinics.doctors_id_seq'::regclass)), 'Bob', 'Hack'),
       ((select nextval('clinics.doctors_id_seq'::regclass)), 'Victor', 'Range');