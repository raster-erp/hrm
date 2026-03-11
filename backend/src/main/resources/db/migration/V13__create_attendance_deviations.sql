CREATE TABLE attendance_deviations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    deviation_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    deviation_minutes INT NOT NULL,
    scheduled_time TIME NOT NULL,
    actual_time TIMESTAMP NOT NULL,
    grace_period_minutes INT NOT NULL DEFAULT 0,
    penalty_action VARCHAR(30) NOT NULL DEFAULT 'NONE',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(500),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deviation_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
