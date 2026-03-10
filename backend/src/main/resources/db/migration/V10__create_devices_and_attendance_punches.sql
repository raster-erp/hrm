CREATE TABLE devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    location VARCHAR(255),
    ip_address VARCHAR(45),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_sync_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_punches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    punch_time TIMESTAMP NOT NULL,
    direction VARCHAR(10) NOT NULL,
    raw_data VARCHAR(500),
    normalized BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(50) NOT NULL DEFAULT 'DEVICE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_punch_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_punch_device FOREIGN KEY (device_id) REFERENCES devices(id)
);

CREATE INDEX idx_punch_employee_time ON attendance_punches(employee_id, punch_time);
CREATE INDEX idx_punch_device ON attendance_punches(device_id);
CREATE INDEX idx_punch_time ON attendance_punches(punch_time);
