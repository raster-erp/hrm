CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    region VARCHAR(100),
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_holidays_date ON holidays(date);
CREATE INDEX idx_holidays_type ON holidays(type);
CREATE INDEX idx_holidays_region ON holidays(region);
CREATE INDEX idx_holidays_active ON holidays(active);

CREATE TABLE leave_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    planned_from_date DATE NOT NULL,
    planned_to_date DATE NOT NULL,
    number_of_days DECIMAL(5,2) NOT NULL,
    notes VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_plans_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_leave_plans_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE INDEX idx_leave_plans_employee ON leave_plans(employee_id);
CREATE INDEX idx_leave_plans_leave_type ON leave_plans(leave_type_id);
CREATE INDEX idx_leave_plans_status ON leave_plans(status);
CREATE INDEX idx_leave_plans_dates ON leave_plans(planned_from_date, planned_to_date);
