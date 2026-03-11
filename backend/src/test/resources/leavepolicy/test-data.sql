-- Clean any existing data (e.g. from V18 seed migration) to avoid UNIQUE constraint violations
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types;

-- Use IDs >= 9000 to avoid conflicts with seed data
INSERT INTO leave_types (id, code, name, category, active, created_at, updated_at) VALUES
(9001, 'CL', 'Casual Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 'SL', 'Sick Leave', 'PAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9003, 'UL', 'Unpaid Leave', 'UNPAID', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days, max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required, active, created_at, updated_at) VALUES
(9001, 'Standard CL Policy', 9001, 'MONTHLY', 1.00, 12.00, 5.00, false, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 'Standard SL Policy', 9002, 'QUARTERLY', 3.00, 12.00, 3.00, true, 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9003, 'Unpaid Leave Policy', 9003, 'ANNUAL', 0.00, null, null, false, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Use department 1 and employee 1 from seed data (V9)
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active, created_at, updated_at) VALUES
(9001, 9001, 'DEPARTMENT', 1, null, null, '2026-01-01', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9002, 9002, 'INDIVIDUAL', null, null, 1, '2026-01-01', '2026-12-31', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
