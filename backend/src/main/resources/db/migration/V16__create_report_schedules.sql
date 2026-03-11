CREATE TABLE report_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    department_id BIGINT,
    recipients VARCHAR(500),
    export_format VARCHAR(10) NOT NULL DEFAULT 'CSV',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_run_at TIMESTAMP,
    next_run_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_report_schedule_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_report_schedule_type ON report_schedules(report_type);
CREATE INDEX idx_report_schedule_active ON report_schedules(active);
