CREATE TABLE separations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    separation_type VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    notice_date DATE,
    last_working_day DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_separation_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE exit_checklists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    separation_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    department VARCHAR(100),
    is_cleared BOOLEAN NOT NULL DEFAULT FALSE,
    cleared_by VARCHAR(100),
    cleared_at TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_checklist_separation FOREIGN KEY (separation_id) REFERENCES separations(id)
);

CREATE TABLE no_dues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    separation_id BIGINT NOT NULL,
    department VARCHAR(100) NOT NULL,
    is_cleared BOOLEAN NOT NULL DEFAULT FALSE,
    cleared_by VARCHAR(100),
    cleared_at TIMESTAMP,
    amount_due DECIMAL(15,2) DEFAULT 0,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nodues_separation FOREIGN KEY (separation_id) REFERENCES separations(id)
);
