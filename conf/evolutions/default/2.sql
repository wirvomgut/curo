# --- !Ups

CREATE TABLE persons (
    id int NOT NULL AUTO_INCREMENT,
    uid varchar(255) NOT NULL UNIQUE
);

CREATE TABLE work_entries (
    id int NOT NULL AUTO_INCREMENT,
    person_id varchar(255) NOT NULL,
    area varchar(255) NOT NULL,
    task varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    time_spent int NOT NULL,
    coins double NOT NULL,
    date_done date NOT NULL
);

# --- !Downs

DROP TABLE persons;
DROP TABLE work_entries;