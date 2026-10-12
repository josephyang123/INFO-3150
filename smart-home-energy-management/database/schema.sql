CREATE DATABASE IF NOT EXISTS smart_db
    CHARACTER SET utf8mb4;
USE smart_db;
-- DROP DATABASE smart_db;


CREATE TABLE user_accounts (
    user_id INT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    passwrd VARCHAR(255) NOT NULL,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	account_role ENUM('FAMILY', 'HOMEOWNER', 'ADMIN') DEFAULT 'FAMILY',

    PRIMARY KEY (user_id),
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
    device_type ENUM('THERMOSTAT', 'SPEAKER', 'DOORBELL', 'LOCK', 'FRIDGE', 'OVEN', 'LIGHT', 'TV', 'KEYCHAIN', 'OTHER')  NOT NULL,
    manufacturer VARCHAR(254),
    powered_state BOOLEAN,
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (device_id),
    CONSTRAINT fk_houseid FOREIGN KEY (house_id) REFERENCES houses(house_id)
);

CREATE TABLE family_members (
	fm_id INT NOT NULL AUTO_INCREMENT,
    house_id INT NOT NULL,
    user_id INT NOT NULL,
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    can_view_device BOOLEAN DEFAULT 0,
    can_edit_device BOOLEAN DEFAULT 0,
    can_view_stats BOOLEAN DEFAULT 0,
    
    PRIMARY KEY (fm_id),
    CONSTRAINT fk_userid_fm FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_houseid_fm FOREIGN KEY (house_id) REFERENCES houses(house_id)
);

INSERT INTO user_accounts(user_name, email, password, account_role) VALUES
    ('John Smith', 'admin@account.com', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'admin'), -- password
    ('Poe Tater', 'po.tat0@mail.com', '923b7bfd85482d2d56a3d385903cb0bd2fef0a6f77011b43627bf2bcdb5ad91e', 'homeowner'), -- potatopotato
    ('Tom A. Toe', 'tomato@mail.com', 'fb0c841846766b1550380971f2d2d48015c16d8e20b3f405672990c3a3d8a112', 'homeowner'), -- tomatotomato
    ('Ms. Fortune', 'luckiestever@luck.com', 'bfbb82ac7ece5654f7494ff3e3aeed1f610698487391c8fc342ff8e81e2330c3', 'family'); -- solucky

INSERT INTO houses(user_id, house_name) VALUES
	(2, 'The FarmHouse'),
    (3, 'No. 3, Garden Row');

INSERT INTO smart_devices(house_id, device_name, device_type, manufacturer, powered_state) VALUES
	(1, 'grow light', 'light', 'IKEA', 0),
    (2, 'Living Room TV', 'tv', 'Samsung', 0),
    (1, 'new keys', 'keychain', 'Smart Keychain Co', 1);

INSERT INTO family_members(house_id, user_id, can_view_device, can_edit_device, can_view_stats) VALUE (1, 4, 1, 1, 0);

/*
SELECT * FROM user_accounts;
SELECT * FROM houses;
SELECT * FROM family_members;
SELECT * FROM smart_devices;
*/
