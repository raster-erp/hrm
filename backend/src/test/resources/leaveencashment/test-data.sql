-- Test data for LeaveEncashment integration tests
-- Uses IDs >= 9800 to avoid conflicts with other test data

DELETE FROM leave_encashments;
DELETE FROM leave_transactions;
DELETE FROM leave_balances;
DELETE FROM leave_approval_logs;
DELETE FROM leave_applications;
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types;

INSERT INTO departments (id, name, code, active) VALUES (9800, 'Test Dept Enc', 'TSTENC', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id, basic_salary)
VALUES (9801, 'ENC-EMP-001', 'John', 'Encash', 'john.encash@test.com', 9800, 30000.00);

INSERT INTO leave_types (id, code, name, category, active, encashable, min_encashment_balance, created_at, updated_at) VALUES
(9801, 'CL-ENC', 'Casual Leave Encashable', 'PAID', true, true, 5.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9802, 'SL-NE', 'Sick Leave Not Encashable', 'PAID', true, false, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_balances (id, employee_id, leave_type_id, "year", credited, used, pending, available, carry_forwarded, encashed, created_at, updated_at)
VALUES (9801, 9801, 9801, YEAR(CURRENT_DATE), 20.00, 3.00, 2.00, 15.00, 0.00, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
