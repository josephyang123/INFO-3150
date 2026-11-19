ALTER TABLE smart_devices
ADD COLUMN automation_threshold_kwh DECIMAL(10, 4) NULL,
    ADD COLUMN automation_comparison ENUM('GT', 'LT') NULL,
    ADD COLUMN automation_last_triggered_date DATE NULL;