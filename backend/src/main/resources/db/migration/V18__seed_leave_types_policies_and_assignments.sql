-- =============================================================
-- V18: Seed leave types, leave policies, and leave policy assignments
-- Defines standard organizational leave types with accrual,
-- carry-forward, lapsing, and maximum accumulation rules
-- =============================================================

-- ────────────────────────────────────────────
-- Leave Types
-- Categories: PAID, UNPAID, STATUTORY, SPECIAL
-- ────────────────────────────────────────────
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

-- ────────────────────────────────────────────
-- Leave Policies
-- Defines accrual frequency, accrual days, max accumulation,
-- carry-forward limits, pro-rata rules, and service eligibility
-- ────────────────────────────────────────────

-- Casual Leave Policy: 1 day per month, max 12 days, no carry-forward (lapse at year-end)
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(1, 'Standard Casual Leave Policy', 1, 'MONTHLY', 1.00,
    12.00, 0.00, TRUE, 0,
    TRUE, 'Monthly accrual of 1 day; maximum 12 days per year; no carry-forward; pro-rata for new joiners');

-- Sick Leave Policy: 0.50 days per month, max 6 days, carry-forward up to 3 days
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(2, 'Standard Sick Leave Policy', 2, 'MONTHLY', 0.50,
    6.00, 3.00, FALSE, 0,
    TRUE, 'Monthly accrual of 0.5 days; maximum 6 days per year; carry-forward up to 3 days');

-- Earned Leave Policy: 1.25 days per month, max 30 days accumulation, carry-forward up to 15 days
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(3, 'Standard Earned Leave Policy', 3, 'MONTHLY', 1.25,
    30.00, 15.00, TRUE, 90,
    TRUE, 'Monthly accrual of 1.25 days; maximum 30 days accumulation; carry-forward up to 15 days; requires 90 days service; pro-rata for new joiners');

-- Maternity Leave Policy: full entitlement credited annually, no carry-forward
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(4, 'Standard Maternity Leave Policy', 4, 'ANNUAL', 182.00,
    182.00, 0.00, FALSE, 80,
    TRUE, 'Annual credit of 182 days (26 weeks); no carry-forward; requires 80 days of service in the preceding 12 months');

-- Paternity Leave Policy: full entitlement credited annually, no carry-forward
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(5, 'Standard Paternity Leave Policy', 5, 'ANNUAL', 15.00,
    15.00, 0.00, FALSE, 90,
    TRUE, 'Annual credit of 15 days; no carry-forward; requires 90 days of service');

-- Unpaid Leave Policy: annual entitlement, no accumulation caps, no carry-forward
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(6, 'Standard Unpaid Leave Policy', 6, 'ANNUAL', 30.00,
    30.00, 0.00, FALSE, 0,
    TRUE, 'Annual allowance of 30 days unpaid leave; no carry-forward; no accumulation beyond limit');

-- Bereavement Leave Policy: annual credit, no carry-forward
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(7, 'Standard Bereavement Leave Policy', 7, 'ANNUAL', 5.00,
    5.00, 0.00, FALSE, 0,
    TRUE, 'Annual credit of 5 days for bereavement; no carry-forward');

-- Marriage Leave Policy: annual credit, no carry-forward
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(8, 'Standard Marriage Leave Policy', 8, 'ANNUAL', 3.00,
    3.00, 0.00, FALSE, 180,
    TRUE, 'Annual credit of 3 days for marriage; requires 180 days of service; no carry-forward');

-- Compensatory Off Policy: quarterly credit, carry-forward up to 2 days
INSERT INTO leave_policies (id, name, leave_type_id, accrual_frequency, accrual_days,
    max_accumulation, carry_forward_limit, pro_rata_for_new_joiners, min_service_days_required,
    active, description) VALUES
(9, 'Standard Comp-Off Policy', 9, 'QUARTERLY', 3.00,
    6.00, 2.00, FALSE, 0,
    TRUE, 'Quarterly accrual of 3 days; maximum 6 days accumulation; carry-forward up to 2 days');

-- ────────────────────────────────────────────
-- Leave Policy Assignments
-- Assign policies to all departments for organization-wide coverage
-- Effective from 2024-01-01 with no end date (ongoing)
-- ────────────────────────────────────────────

-- Casual Leave – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(1, 1, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(2, 1, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(3, 1, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(4, 1, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(5, 1, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(6, 1, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(7, 1, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(8, 1, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(9, 1, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(10, 1, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);

-- Sick Leave – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(11, 2, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(12, 2, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(13, 2, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(14, 2, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(15, 2, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(16, 2, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(17, 2, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(18, 2, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(19, 2, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(20, 2, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);

-- Earned Leave – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(21, 3, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(22, 3, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(23, 3, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(24, 3, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(25, 3, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(26, 3, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(27, 3, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(28, 3, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(29, 3, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(30, 3, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);

-- Unpaid Leave – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(31, 6, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(32, 6, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(33, 6, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(34, 6, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(35, 6, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(36, 6, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(37, 6, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(38, 6, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(39, 6, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(40, 6, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);

-- Bereavement Leave – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(41, 7, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(42, 7, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(43, 7, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(44, 7, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(45, 7, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(46, 7, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(47, 7, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(48, 7, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(49, 7, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(50, 7, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);

-- Compensatory Off – all departments
INSERT INTO leave_policy_assignments (id, leave_policy_id, assignment_type, department_id, designation_id, employee_id, effective_from, effective_to, active) VALUES
(51, 9, 'DEPARTMENT', 1, NULL, NULL, '2024-01-01', NULL, TRUE),
(52, 9, 'DEPARTMENT', 2, NULL, NULL, '2024-01-01', NULL, TRUE),
(53, 9, 'DEPARTMENT', 3, NULL, NULL, '2024-01-01', NULL, TRUE),
(54, 9, 'DEPARTMENT', 4, NULL, NULL, '2024-01-01', NULL, TRUE),
(55, 9, 'DEPARTMENT', 5, NULL, NULL, '2024-01-01', NULL, TRUE),
(56, 9, 'DEPARTMENT', 6, NULL, NULL, '2024-01-01', NULL, TRUE),
(57, 9, 'DEPARTMENT', 7, NULL, NULL, '2024-01-01', NULL, TRUE),
(58, 9, 'DEPARTMENT', 8, NULL, NULL, '2024-01-01', NULL, TRUE),
(59, 9, 'DEPARTMENT', 9, NULL, NULL, '2024-01-01', NULL, TRUE),
(60, 9, 'DEPARTMENT', 10, NULL, NULL, '2024-01-01', NULL, TRUE);
