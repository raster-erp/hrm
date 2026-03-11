CREATE TABLE regularization_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    type VARCHAR(30) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    original_punch_in TIMESTAMP,
    original_punch_out TIMESTAMP,
    corrected_punch_in TIMESTAMP NOT NULL,
    corrected_punch_out TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approval_level INT NOT NULL DEFAULT 0,
    remarks VARCHAR(500),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
