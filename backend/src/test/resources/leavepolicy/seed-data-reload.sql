-- Reload V18 seed data for testing; clean existing data first to avoid constraint violations
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types;

INSERT INTO leave_types (id, code, name, category, description, active) VALUES
(1, 'CL', 'Casual Leave', 'PAID', 'Short-duration paid leave for personal or unforeseen needs', TRUE),
(2, 'SL', 'Sick Leave', 'PAID', 'Paid leave for illness or medical treatment', TRUE),
(3, 'EL', 'Earned Leave', 'PAID', 'Accrued paid leave based on service; also known as annual or privilege leave', TRUE),
(4, 'ML', 'Maternity Leave', 'STATUTORY', 'Statutory paid leave for female employees before and after childbirth (26 weeks)', TRUE),
(5, 'PL', 'Paternity Leave', 'STATUTORY', 'Statutory paid leave for male employees after childbirth (15 days)', TRUE),
(6, 'UL', 'Unpaid Leave', 'UNPAID', 'Leave without pay when paid leave balance is exhausted', TRUE),
(7, 'BL', 'Bereavement Leave', 'SPECIAL', 'Leave granted for mourning the death of an immediate family member', TRUE),
(8, 'MRL', 'Marriage Leave', 'SPECIAL', 'Special leave granted for the employee''s own marriage', TRUE),
(9, 'CO', 'Compensatory Off', 'PAID', 'Paid leave earned by working on holidays or weekends', TRUE);

INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(1, 'Standard Casual Leave Policy', 1, 'MONTHLY', 1.00, 12.00, 0.00, TRUE, 0, TRUE,
    'Monthly accrual of 1 day; maximum 12 days per year; no carry-forward; pro-rata for new joiners'),
(2, 'Standard Sick Leave Policy', 2, 'MONTHLY', 0.50, 6.00, 3.00, FALSE, 0, TRUE,
    'Monthly accrual of 0.5 days; maximum 6 days per year; carry-forward up to 3 days'),
(3, 'Standard Earned Leave Policy', 3, 'MONTHLY', 1.25, 30.00, 15.00, TRUE, 90, TRUE,
    'Monthly accrual of 1.25 days; maximum 30 days accumulation; carry-forward up to 15 days'),
(4, 'Standard Maternity Leave Policy', 4, 'ANNUAL', 182.00, 182.00, 0.00, FALSE, 80, TRUE,
    'Annual credit of 182 days (26 weeks); no carry-forward; requires 80 days of service'),
(5, 'Standard Paternity Leave Policy', 5, 'ANNUAL', 15.00, 15.00, 0.00, FALSE, 90, TRUE,
    'Annual credit of 15 days; no carry-forward; requires 90 days of service'),
(6, 'Standard Unpaid Leave Policy', 6, 'ANNUAL', 30.00, 30.00, 0.00, FALSE, 0, TRUE,
    'Annual allowance of 30 days unpaid leave; no carry-forward'),
(7, 'Standard Bereavement Leave Policy', 7, 'ANNUAL', 5.00, 5.00, 0.00, FALSE, 0, TRUE,
    'Annual credit of 5 days for bereavement; no carry-forward'),
(8, 'Standard Marriage Leave Policy', 8, 'ANNUAL', 3.00, 3.00, 0.00, FALSE, 180, TRUE,
    'Annual credit of 3 days for marriage; requires 180 days of service'),
(9, 'Standard Comp-Off Policy', 9, 'QUARTERLY', 3.00, 6.00, 2.00, FALSE, 0, TRUE,
    'Quarterly accrual of 3 days; maximum 6 days accumulation; carry-forward up to 2 days');

-- Casual Leave assignments (policy 1) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(1, 1, 'DEPARTMENT', 1, '2024-01-01', TRUE), (2, 1, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(3, 1, 'DEPARTMENT', 3, '2024-01-01', TRUE), (4, 1, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(5, 1, 'DEPARTMENT', 5, '2024-01-01', TRUE), (6, 1, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(7, 1, 'DEPARTMENT', 7, '2024-01-01', TRUE), (8, 1, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(9, 1, 'DEPARTMENT', 9, '2024-01-01', TRUE), (10, 1, 'DEPARTMENT', 10, '2024-01-01', TRUE);

-- Sick Leave assignments (policy 2) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(11, 2, 'DEPARTMENT', 1, '2024-01-01', TRUE), (12, 2, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(13, 2, 'DEPARTMENT', 3, '2024-01-01', TRUE), (14, 2, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(15, 2, 'DEPARTMENT', 5, '2024-01-01', TRUE), (16, 2, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(17, 2, 'DEPARTMENT', 7, '2024-01-01', TRUE), (18, 2, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(19, 2, 'DEPARTMENT', 9, '2024-01-01', TRUE), (20, 2, 'DEPARTMENT', 10, '2024-01-01', TRUE);

-- Earned Leave assignments (policy 3) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(21, 3, 'DEPARTMENT', 1, '2024-01-01', TRUE), (22, 3, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(23, 3, 'DEPARTMENT', 3, '2024-01-01', TRUE), (24, 3, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(25, 3, 'DEPARTMENT', 5, '2024-01-01', TRUE), (26, 3, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(27, 3, 'DEPARTMENT', 7, '2024-01-01', TRUE), (28, 3, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(29, 3, 'DEPARTMENT', 9, '2024-01-01', TRUE), (30, 3, 'DEPARTMENT', 10, '2024-01-01', TRUE);

-- Unpaid Leave assignments (policy 6) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(31, 6, 'DEPARTMENT', 1, '2024-01-01', TRUE), (32, 6, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(33, 6, 'DEPARTMENT', 3, '2024-01-01', TRUE), (34, 6, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(35, 6, 'DEPARTMENT', 5, '2024-01-01', TRUE), (36, 6, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(37, 6, 'DEPARTMENT', 7, '2024-01-01', TRUE), (38, 6, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(39, 6, 'DEPARTMENT', 9, '2024-01-01', TRUE), (40, 6, 'DEPARTMENT', 10, '2024-01-01', TRUE);

-- Bereavement Leave assignments (policy 7) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(41, 7, 'DEPARTMENT', 1, '2024-01-01', TRUE), (42, 7, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(43, 7, 'DEPARTMENT', 3, '2024-01-01', TRUE), (44, 7, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(45, 7, 'DEPARTMENT', 5, '2024-01-01', TRUE), (46, 7, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(47, 7, 'DEPARTMENT', 7, '2024-01-01', TRUE), (48, 7, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(49, 7, 'DEPARTMENT', 9, '2024-01-01', TRUE), (50, 7, 'DEPARTMENT', 10, '2024-01-01', TRUE);

-- Compensatory Off assignments (policy 9) to all 10 departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, effective_from, active) VALUES
(51, 9, 'DEPARTMENT', 1, '2024-01-01', TRUE), (52, 9, 'DEPARTMENT', 2, '2024-01-01', TRUE),
(53, 9, 'DEPARTMENT', 3, '2024-01-01', TRUE), (54, 9, 'DEPARTMENT', 4, '2024-01-01', TRUE),
(55, 9, 'DEPARTMENT', 5, '2024-01-01', TRUE), (56, 9, 'DEPARTMENT', 6, '2024-01-01', TRUE),
(57, 9, 'DEPARTMENT', 7, '2024-01-01', TRUE), (58, 9, 'DEPARTMENT', 8, '2024-01-01', TRUE),
(59, 9, 'DEPARTMENT', 9, '2024-01-01', TRUE), (60, 9, 'DEPARTMENT', 10, '2024-01-01', TRUE);
