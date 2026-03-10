CREATE TABLE shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    break_duration_minutes INT DEFAULT 0,
    grace_period_minutes INT DEFAULT 0,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rotation_patterns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    rotation_days INT NOT NULL,
    shift_sequence VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_rosters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    shift_id BIGINT NOT NULL,
    effective_date DATE NOT NULL,
    end_date DATE,
    rotation_pattern_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roster_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_roster_shift FOREIGN KEY (shift_id) REFERENCES shifts(id),
    CONSTRAINT fk_roster_pattern FOREIGN KEY (rotation_pattern_id) REFERENCES rotation_patterns(id)
);

CREATE INDEX idx_roster_employee ON shift_rosters(employee_id);
CREATE INDEX idx_roster_shift ON shift_rosters(shift_id);
CREATE INDEX idx_roster_effective_date ON shift_rosters(effective_date);
CREATE INDEX idx_roster_employee_dates ON shift_rosters(employee_id, effective_date, end_date);
