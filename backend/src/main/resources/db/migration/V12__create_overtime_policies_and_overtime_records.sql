-- Overtime Policies: defines rate multipliers and caps for different overtime types
CREATE TABLE overtime_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    rate_multiplier DECIMAL(4,2) NOT NULL,
    min_overtime_minutes INT NOT NULL DEFAULT 0,
    max_overtime_minutes_per_day INT,
    max_overtime_minutes_per_month INT,
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Overtime Records: tracks employee overtime hours with approval workflow
CREATE TABLE overtime_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    overtime_date DATE NOT NULL,
    overtime_policy_id BIGINT NOT NULL,
    overtime_minutes INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    shift_start_time TIME,
    shift_end_time TIME,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    remarks VARCHAR(500),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_overtime_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_overtime_policy FOREIGN KEY (overtime_policy_id) REFERENCES overtime_policies(id)
);
