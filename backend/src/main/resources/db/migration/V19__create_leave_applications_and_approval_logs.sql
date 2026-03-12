CREATE TABLE leave_applications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT         NOT NULL,
    leave_type_id BIGINT         NOT NULL,
    from_date     DATE           NOT NULL,
    to_date       DATE           NOT NULL,
    number_of_days DECIMAL(5, 2) NOT NULL,
    reason        VARCHAR(500),
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    approval_level INT           NOT NULL DEFAULT 0,
    remarks       VARCHAR(500),
    approved_by   VARCHAR(100),
    approved_at   TIMESTAMP,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_app_employee   FOREIGN KEY (employee_id)   REFERENCES employees (id),
    CONSTRAINT fk_leave_app_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types (id)
);

CREATE TABLE leave_approval_logs (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    leave_application_id BIGINT      NOT NULL,
    approver_name        VARCHAR(100),
    approval_level       INT         NOT NULL,
    action               VARCHAR(20) NOT NULL,
    remarks              VARCHAR(500),
    created_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_leave_app FOREIGN KEY (leave_application_id) REFERENCES leave_applications (id)
);
