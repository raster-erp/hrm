-- Comp-Off Management

CREATE TABLE comp_off_credits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    worked_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    credit_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    hours_worked DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    used_date DATE,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_compoff_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_compoff_employee ON comp_off_credits (employee_id);
CREATE INDEX idx_compoff_status ON comp_off_credits (status);
CREATE INDEX idx_compoff_worked_date ON comp_off_credits (worked_date);
CREATE INDEX idx_compoff_expiry_date ON comp_off_credits (expiry_date);
