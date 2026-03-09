CREATE TABLE credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    credential_type VARCHAR(50) NOT NULL,
    credential_name VARCHAR(100) NOT NULL,
    issuer VARCHAR(200),
    issue_date DATE,
    expiry_date DATE,
    credential_number VARCHAR(100),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credential_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE credential_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_credential FOREIGN KEY (credential_id) REFERENCES credentials(id)
);
