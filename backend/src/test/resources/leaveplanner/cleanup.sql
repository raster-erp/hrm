DELETE FROM leave_plans WHERE employee_id IN (9801, 9802);
DELETE FROM holidays WHERE id >= 9800;
DELETE FROM leave_applications WHERE id >= 9800;
DELETE FROM leave_types WHERE id >= 9800;
DELETE FROM employees WHERE id IN (9801, 9802);
DELETE FROM departments WHERE id = 9800;
