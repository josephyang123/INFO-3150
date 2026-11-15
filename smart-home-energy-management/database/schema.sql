-- Fresh installation only. Do not run this script against an existing database.
-- Accounts, houses, and devices are created through the application.
CREATE DATABASE IF NOT EXISTS smart_db
    CHARACTER SET utf8mb4;
USE smart_db;
CREATE TABLE user_accounts (
    user_id INT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password VARCHAR(255) NOT NULL,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	account_role ENUM('FAMILY', 'HOMEOWNER', 'ADMIN') DEFAULT 'FAMILY',
    session_version BIGINT NOT NULL DEFAULT 0,

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
    powered_state BOOLEAN default 0,
    power_watts DECIMAL(8, 2) NULL,
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
    CONSTRAINT uq_family_members_house_user UNIQUE (house_id, user_id),
    CONSTRAINT fk_userid_fm FOREIGN KEY (user_id) REFERENCES user_accounts(user_id),
    CONSTRAINT fk_houseid_fm FOREIGN KEY (house_id) REFERENCES houses(house_id)
);

CREATE TABLE device_usage_sessions (
    session_id INT NOT NULL AUTO_INCREMENT,
    device_id INT NULL,
    house_id INT NOT NULL,
    power_watts DECIMAL(8, 2) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    ended_at DATETIME(6) NULL,

    PRIMARY KEY (session_id),
    INDEX idx_usage_house_start (house_id, started_at),
    INDEX idx_usage_device_end (device_id, ended_at),
    FOREIGN KEY (device_id) REFERENCES smart_devices(device_id)
        ON DELETE SET NULL,
    FOREIGN KEY (house_id) REFERENCES houses(house_id)
);
