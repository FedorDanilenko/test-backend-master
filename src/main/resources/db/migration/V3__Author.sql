CREATE TABLE author
(
    id           serial primary key,
    fio          text not null,
    date_Created date not null
);