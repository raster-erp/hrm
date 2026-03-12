-- Test data for leave planner integration tests
-- Using IDs >= 9800 to avoid conflicts

INSERT INTO departments (id, name, code, created_at, updated_at) VALUES (9800, 'Test Dept Planner', 'TSTPL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO employees (id, employee_code, first_name, last_name, email, phone, date_of_birth, gender, joining_date, employment_status, department_id, created_at, updated_at, deleted)
VALUES (9801, 'PL-EMP-001', 'Alice', 'Planner', 'alice.planner@test.com', '9876543210', '1990-01-15', 'FEMALE', '2020-01-01', 'ACTIVE', 9800, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

INSERT INTO employees (id, employee_code, first_name, last_name, email, phone, date_of_birth, gender, joining_date, employment_status, department_id, created_at, updated_at, deleted)
VALUES (9802, 'PL-EMP-002', 'Bob', 'Calendar', 'bob.calendar@test.com', '9876543211', '1991-02-20', 'MALE', '2020-02-01', 'ACTIVE', 9800, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

INSERT INTO leave_types (id, code, name, category, description, encashable, min_encashment_balance, active, created_at, updated_at)
VALUES (9801, 'PL-CL', 'Planner Casual Leave', 'PAID', 'Test casual leave for planner', FALSE, 0, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- A pre-existing approved leave application for calendar testing
INSERT INTO leave_applications (id, employee_id, leave_type_id, from_date, to_date, number_of_days, reason, status, approval_level, created_at, updated_at)
VALUES (9801, 9801, 9801, '2026-03-20', '2026-03-22', 3.00, 'Family vacation', 'APPROVED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
