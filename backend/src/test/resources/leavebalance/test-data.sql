-- Test data for LeaveBalance integration tests
-- Uses IDs >= 9000 to avoid conflicts with seed data

DELETE FROM leave_transactions;
DELETE FROM leave_balances;
DELETE FROM leave_approval_logs;
DELETE FROM leave_applications;
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types;

INSERT INTO departments (id, name, code, active) VALUES (9001, 'Test Dept LB', 'TSTLB', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9001, 'LB-EMP-001', 'John', 'Doe', 'john.doe.lb@test.com', 9001);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9002, 'LB-EMP-002', 'Jane', 'Smith', 'jane.smith.lb@test.com', 9001);

INSERT INTO leave_types (id, code, name, category, active, created_at, updated_at) VALUES
(9001, 'CL', 'Casual Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 'SL', 'Sick Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days, max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required, active, created_at, updated_at)
VALUES (9001, 'Test CL Policy', 9001, 'ANNUAL', 12, 20, 5, false, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active, created_at, updated_at)
VALUES (9001, 9001, 'DEPARTMENT', 9001, '2024-01-01', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_balances (id, employee_id, leave_type_id, "year", credited, used, pending, available, carry_forwarded, encashed, created_at, updated_at)
VALUES (9001, 9001, 9001, 2025, 12.00, 3.00, 2.00, 7.00, 0.00, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_balances (id, employee_id, leave_type_id, "year", credited, used, pending, available, carry_forwarded, encashed, created_at, updated_at)
VALUES (9002, 9001, 9002, 2025, 6.00, 1.00, 0.00, 5.00, 0.00, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_balances (id, employee_id, leave_type_id, "year", credited, used, pending, available, carry_forwarded, encashed, created_at, updated_at)
VALUES (9003, 9002, 9001, 2025, 12.00, 4.00, 0.00, 8.00, 0.00, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
