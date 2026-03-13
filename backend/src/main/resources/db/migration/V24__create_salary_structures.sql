-- Salary Structure Configuration

CREATE TABLE salary_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    computation_type VARCHAR(20) NOT NULL,
    percentage_value DECIMAL(5,2),
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE salary_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE salary_structure_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salary_structure_id BIGINT NOT NULL,
    salary_component_id BIGINT NOT NULL,
    computation_type VARCHAR(20) NOT NULL,
    percentage_value DECIMAL(5,2),
    fixed_amount DECIMAL(12,2),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_structure_comp_structure FOREIGN KEY (salary_structure_id) REFERENCES salary_structures(id),
    CONSTRAINT fk_structure_comp_component FOREIGN KEY (salary_component_id) REFERENCES salary_components(id),
    CONSTRAINT uq_structure_component UNIQUE (salary_structure_id, salary_component_id)
);

CREATE TABLE employee_salary_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    salary_structure_id BIGINT NOT NULL,
    ctc DECIMAL(12,2) NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    effective_date DATE NOT NULL,
    notes VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_salary_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_emp_salary_structure FOREIGN KEY (salary_structure_id) REFERENCES salary_structures(id)
);

CREATE INDEX idx_emp_salary_employee ON employee_salary_details(employee_id);
CREATE INDEX idx_emp_salary_effective_date ON employee_salary_details(effective_date);
