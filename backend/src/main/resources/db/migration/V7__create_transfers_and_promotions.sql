CREATE TABLE transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    from_department_id BIGINT,
    to_department_id BIGINT,
    from_branch VARCHAR(100),
    to_branch VARCHAR(100),
    transfer_type VARCHAR(30) NOT NULL,
    effective_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_transfer_from_dept FOREIGN KEY (from_department_id) REFERENCES departments(id),
    CONSTRAINT fk_transfer_to_dept FOREIGN KEY (to_department_id) REFERENCES departments(id)
);

CREATE TABLE promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    old_designation_id BIGINT,
    new_designation_id BIGINT,
    old_grade VARCHAR(20),
    new_grade VARCHAR(20),
    effective_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_promotion_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_promotion_old_desig FOREIGN KEY (old_designation_id) REFERENCES designations(id),
    CONSTRAINT fk_promotion_new_desig FOREIGN KEY (new_designation_id) REFERENCES designations(id)
);
