# --- !Ups

CREATE TABLE persons (
    id INT AUTO_INCREMENT primary key NOT NULL,
    uid varchar(255) NOT NULL UNIQUE
);

CREATE TABLE work_entries (
    id INT AUTO_INCREMENT primary key NOT NULL,
    person_id varchar(255) NOT NULL,
    area varchar(255) NOT NULL,
    task varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    time_spent int NOT NULL,
    coins double NOT NULL,
    date_done date NOT NULL,
    date_created DATETIME NOT NULL DEFAULT(GETDATE())
);

# --- !Downs

DROP TABLE persons;
DROP TABLE work_entries;