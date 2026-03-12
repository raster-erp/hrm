-- Test data for LeaveApplication integration tests
-- Uses IDs >= 9000 to avoid conflicts with seed data

-- Clean any existing leave_applications and approval_logs first
DELETE FROM leave_transactions;
DELETE FROM leave_balances;
DELETE FROM leave_approval_logs;
DELETE FROM leave_applications;

-- Clean existing leave policy data to avoid UNIQUE constraint violations
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types;

INSERT INTO departments (id, name, code, active) VALUES (9001, 'Test Dept LA', 'TSTLA', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9001, 'LA-EMP-001', 'John', 'Doe', 'john.doe.la@test.com', 9001);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9002, 'LA-EMP-002', 'Jane', 'Smith', 'jane.smith.la@test.com', 9001);

INSERT INTO leave_types (id, code, name, category, active, created_at, updated_at) VALUES
(9001, 'CL', 'Casual Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 'SL', 'Sick Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
