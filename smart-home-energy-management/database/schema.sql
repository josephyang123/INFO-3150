CREATE DATABASE IF NOT EXISTS smart_db
    CHARACTER SET utf8mb4;

USE smart_db;

CREATE TABLE user_accounts (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(254) NOT NULL,
    passwrd VARCHAR(255) NOT NULL,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	account_role VARCHAR(10) DEFAULT 'FAMILY',

    PRIMARY KEY (user_id),
    UNIQUE (username),
    UNIQUE (email)
);

CREATE TABLE houses (
	house_id INT NOT NULL AUTO_INCREMENT,
	user_id INT NOT NULL,
	house_name VARCHAR(254) NOT NULL,
    date_created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
	PRIMARY KEY (house_id),
    CONSTRAINT fk_userid
    FOREIGN KEY (user_id) REFERENCES user_accounts(user_id)
    );
    
CREATE TABLE smart_devices (
	device_id INT NOT NULL AUTO_INCREMENT,
	house_id INT NOT NULL,
    device_name VARCHAR(254) NOT NULL,
    device_type VARCHAR(254) NOT NULL,
    manufacturer VARCHAR(254) NOT NULL,
    powered_state TINYINT(1) DEFAULT 0,
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (device_id),
    CONSTRAINT fk_houseid FOREIGN KEY (house_id) REFERENCES houses(house_id)
);

CREATE TABLE family_members (
	fm_id INT NOT NULL AUTO_INCREMENT,
    house_id INT NOT NULL,
    user_id INT NOT NULL,
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (fm_id)
    CONSTRAINT fk_userid FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_houseid FOREIGN KEY (house_id) REFERENCES houses(house_id)
);
