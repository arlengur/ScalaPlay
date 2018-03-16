# --- !Ups

CREATE TABLE BOOK (
  ID integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  TITLE varchar(50) NOT NULL,
  PRICE integer NOT NULL,
  AUTHOR varchar(50) NOT NULL,
);

# --- !Downs
DROP TABLE BOOK if exists;