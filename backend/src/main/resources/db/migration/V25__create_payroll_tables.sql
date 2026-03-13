-- Monthly Payroll Computation

CREATE TABLE payroll_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    run_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_gross DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_deductions DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_net DECIMAL(14,2) NOT NULL DEFAULT 0,
    employee_count INT NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payroll_period UNIQUE (period_year, period_month)
);

CREATE TABLE payroll_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_run_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    salary_structure_id BIGINT NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    gross_salary DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_salary DECIMAL(12,2) NOT NULL DEFAULT 0,
    component_breakup TEXT,
    days_payable INT NOT NULL DEFAULT 30,
    lop_days INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pd_payroll_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs(id),
    CONSTRAINT fk_pd_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_pd_salary_structure FOREIGN KEY (salary_structure_id) REFERENCES salary_structures(id),
    CONSTRAINT uq_payroll_detail_employee UNIQUE (payroll_run_id, employee_id)
);

CREATE TABLE payroll_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_run_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    adjustment_type VARCHAR(20) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pa_payroll_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs(id),
    CONSTRAINT fk_pa_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_payroll_details_run ON payroll_details(payroll_run_id);
CREATE INDEX idx_payroll_details_employee ON payroll_details(employee_id);
CREATE INDEX idx_payroll_adjustments_run ON payroll_adjustments(payroll_run_id);
