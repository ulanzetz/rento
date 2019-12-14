create table accounts
(
    phone         varchar(12) not null,
    name          varchar(30) not null,
    password_hash varchar(40) not null
);

create unique index accounts_phone_uindex
    on accounts (phone);

alter table accounts
    add constraint accounts_pk
        primary key (phone);


create table restaurants
(
    id           serial           not null
        constraint restaurants_pk
            primary key,
    name         varchar(100)     not null,
    description  varchar(300)     not null,
    photo        varchar(300)     not null,
    address      varchar(100)     not null,
    rating       double precision not null,
    phone        varchar(30)      not null,
    middle_check int              not null,
);

create unique index restaurants_id_uindex
    on restaurants (id);


create table restaurant_schedule
(
    restaurant_id int  not null,
    weekday       int  not null,
    start_time    time not null,
    end_time      time not null
);

alter table restaurant_schedule
    add constraint restaurant_schedule_pk
        primary key (restaurant_id, weekday, start_time, end_time);

alter table restaurant_schedule
    add constraint restaurant_schedule_restaurants_id_fk
        foreign key (restaurant_id) references restaurants;


create table orders
(
    id            serial                not null,
    phone         varchar(12)           not null
        constraint orders_accounts_phone_fk
            references accounts,
    name          varchar(30)           not null,
    person_count  int                   not null,
    datetime      timestamp             not null,
    restaurant_id int                   not null
        constraint orders_restaurants_id_fk
            references restaurants,
    canceled      boolean default false not null
);

create unique index orders_id_uindex
    on orders (id);

alter table orders
    add constraint orders_pk
        primary key (id);

alter table orders
    add constraint orders_pk_2
        unique (phone, datetime);