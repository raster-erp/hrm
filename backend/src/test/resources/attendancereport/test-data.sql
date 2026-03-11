-- Test data for Attendance Report integration tests
-- Uses IDs >= 9701 to avoid conflicts

INSERT INTO departments (id, name, code, active) VALUES (9701, 'Report Test Dept', 'TSTDPT-RPT', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9701, 'EMP-RPT-001', 'Alice', 'Report', 'alice.report@test.com', 9701);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9702, 'EMP-RPT-002', 'Bob', 'Report', 'bob.report@test.com', 9701);

-- Attendance punches for employee 9701 on a weekday (Monday 2025-06-02)
INSERT INTO devices (id, serial_number, name, type, location, status)
VALUES (9701, 'DEV-RPT-001', 'Test Device', 'BIOMETRIC', 'Main Gate', 'ACTIVE');

INSERT INTO attendance_punches (id, employee_id, device_id, punch_time, direction, normalized, source)
VALUES (9701, 9701, 9701, '2025-06-02 09:00:00', 'IN', true, 'TEST');

INSERT INTO attendance_punches (id, employee_id, device_id, punch_time, direction, normalized, source)
VALUES (9702, 9701, 9701, '2025-06-02 18:00:00', 'OUT', true, 'TEST');

-- Employee 9702 has NO punches on 2025-06-02 (absent)
