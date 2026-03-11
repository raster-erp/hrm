-- WFH Requests: tracks work-from-home requests with approval workflow
CREATE TABLE wfh_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wfh_request_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- WFH Activity Logs: tracks check-in/check-out for WFH days
CREATE TABLE wfh_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wfh_request_id BIGINT NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    ip_address VARCHAR(45),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wfh_activity_request FOREIGN KEY (wfh_request_id) REFERENCES wfh_requests(id)
);
