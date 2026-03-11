-- Leave types: defines categories of leave (e.g. Annual, Sick, Maternity)
CREATE TABLE leave_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Leave policies: accrual and accumulation rules tied to a leave type
CREATE TABLE leave_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    leave_type_id BIGINT NOT NULL,
    accrual_frequency VARCHAR(20) NOT NULL,
    accrual_days DECIMAL(5,2) NOT NULL,
    max_accumulation DECIMAL(5,2),
    carry_forward_limit DECIMAL(5,2),
    pro_rata_for_new_joiners BOOLEAN NOT NULL DEFAULT FALSE,
    min_service_days_required INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_leave_policy_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

-- Leave policy assignments: links policies to departments, designations, or individual employees
CREATE TABLE leave_policy_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_policy_id BIGINT NOT NULL,
    assignment_type VARCHAR(20) NOT NULL,
    department_id BIGINT,
    designation_id BIGINT,
    employee_id BIGINT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_lpa_leave_policy FOREIGN KEY (leave_policy_id) REFERENCES leave_policies(id),
    CONSTRAINT fk_lpa_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_lpa_designation FOREIGN KEY (designation_id) REFERENCES designations(id),
    CONSTRAINT fk_lpa_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
