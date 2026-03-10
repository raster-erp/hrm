-- Test data for Overtime integration tests
-- Uses IDs >= 9001 to avoid conflicts with seed data

INSERT INTO departments (id, name, code, active) VALUES (9001, 'Test Dept', 'TSTDPT-OT', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9001, 'EMP-OT-001', 'John', 'Doe', 'john.doe.ot@test.com', 9001);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9002, 'EMP-OT-002', 'Jane', 'Smith', 'jane.smith.ot@test.com', 9001);

INSERT INTO shifts (id, name, type, start_time, end_time, break_duration_minutes, active)
VALUES (9001, 'OT Morning Shift', 'MORNING', '09:00:00', '17:00:00', 60, true);

INSERT INTO overtime_policies (id, name, type, rate_multiplier, min_overtime_minutes, max_overtime_minutes_per_day, requires_approval, active)
VALUES (9001, 'Weekday OT', 'WEEKDAY', 1.50, 30, 240, true, true);

INSERT INTO overtime_policies (id, name, type, rate_multiplier, min_overtime_minutes, requires_approval, active)
VALUES (9002, 'Weekend OT', 'WEEKEND', 2.00, 0, true, true);

-- Shift roster for employee 9001 covering 2025
INSERT INTO shift_rosters (id, employee_id, shift_id, effective_date, end_date)
VALUES (9001, 9001, 9001, '2025-01-01', '2025-12-31');

-- Device for attendance punches
INSERT INTO devices (id, serial_number, name, type, status)
VALUES (9001, 'DEV-OT-001', 'OT Test Device', 'BIOMETRIC', 'ACTIVE');

-- Attendance punches for employee 9001 on 2025-03-10 (worked 09:00-19:30 = 10.5 hrs, shift is 8 hrs with 1 hr break)
INSERT INTO attendance_punches (id, employee_id, device_id, punch_time, direction, source)
VALUES (9001, 9001, 9001, '2025-03-10 08:55:00', 'IN', 'DEVICE');

INSERT INTO attendance_punches (id, employee_id, device_id, punch_time, direction, source)
VALUES (9002, 9001, 9001, '2025-03-10 19:30:00', 'OUT', 'DEVICE');
