DELETE FROM leave_approval_logs;
DELETE FROM leave_applications;
DELETE FROM leave_policy_assignments;
DELETE FROM leave_policies;
DELETE FROM leave_types WHERE id IN (9001, 9002);
DELETE FROM employees WHERE id IN (9001, 9002);
DELETE FROM departments WHERE id = 9001;
