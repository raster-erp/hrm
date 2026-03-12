DELETE FROM leave_transactions;
DELETE FROM leave_balances;
DELETE FROM leave_approval_logs;
DELETE FROM leave_applications;
DELETE FROM leave_policy_assignments WHERE id >= 9000;
DELETE FROM leave_policies WHERE id >= 9000;
DELETE FROM leave_types WHERE id IN (9001, 9002);
DELETE FROM employees WHERE id IN (9001, 9002);
DELETE FROM departments WHERE id = 9001;
