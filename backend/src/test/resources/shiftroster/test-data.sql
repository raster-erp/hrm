-- Test data for ShiftRoster integration tests

INSERT INTO departments (id, name, code, active) VALUES (9001, 'Test Dept', 'TSTDPT', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9001, 'EMP-001', 'John', 'Doe', 'john.doe.sr@test.com', 9001);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9002, 'EMP-002', 'Jane', 'Smith', 'jane.smith.sr@test.com', 9001);

INSERT INTO shifts (id, name, type, start_time, end_time, active)
VALUES (9001, 'Morning Shift', 'MORNING', '06:00:00', '14:00:00', true);

INSERT INTO shifts (id, name, type, start_time, end_time, active)
VALUES (9002, 'Evening Shift', 'EVENING', '14:00:00', '22:00:00', true);

INSERT INTO rotation_patterns (id, name, description, rotation_days, shift_sequence)
VALUES (9001, 'Weekly Rotation', 'Rotates every week', 7, '1,2');
