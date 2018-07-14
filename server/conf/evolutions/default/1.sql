# --- !Ups

CREATE TABLE users (
    loginInfoId varchar(255) NOT NULL,
    loginInfoKey varchar(255) NOT NULL,
    firstName varchar(255),
    lastName varchar(255),
    fullName varchar(255),
    email varchar(255)
);

CREATE TABLE authinfo (
    loginInfoId varchar(255) NOT NULL,
    loginInfoKey varchar(255) NOT NULL,
    hasher varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    salt varchar(255)
);

# --- !Downs

DROP TABLE users;
DROP TABLE authinfo;