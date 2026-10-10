CREATE DATABASE IF NOT EXISTS smart_db
    CHARACTER SET utf8mb4;

USE smart_db;

CREATE TABLE user_accounts (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password VARCHAR(255) NOT NULL,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE (username),
    UNIQUE (email)
);