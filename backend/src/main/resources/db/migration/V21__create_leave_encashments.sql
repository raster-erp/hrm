-- Leave Encashment Management

-- Add encashable flag and minimum balance for encashment to leave_types
ALTER TABLE leave_types ADD COLUMN encashable BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE leave_types ADD COLUMN min_encashment_balance DECIMAL(5,2) DEFAULT 0;

-- Add basic_salary to employees for encashment calculation
ALTER TABLE employees ADD COLUMN basic_salary DECIMAL(12,2) DEFAULT 0;

-- Add encashed column to leave_balances
ALTER TABLE leave_balances ADD COLUMN encashed DECIMAL(7,2) NOT NULL DEFAULT 0;

-- Leave Encashment requests table
CREATE TABLE leave_encashments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    "year" INT NOT NULL,
    number_of_days DECIMAL(5,2) NOT NULL,
    per_day_salary DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(14,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_encashment_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_encashment_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE INDEX idx_encashment_employee ON leave_encashments (employee_id);
CREATE INDEX idx_encashment_leave_type ON leave_encashments (leave_type_id);
CREATE INDEX idx_encashment_status ON leave_encashments (status);
CREATE INDEX idx_encashment_year ON leave_encashments ("year");
