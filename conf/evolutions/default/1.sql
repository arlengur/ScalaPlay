# --- !Ups

CREATE TABLE BOOK (
  ID     INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY,
  TITLE  VARCHAR(50) NOT NULL,
  PRICE  INTEGER     NOT NULL,
  AUTHOR VARCHAR(50) NOT NULL,
);

# --- !Downs
DROP TABLE BOOK IF EXISTS;