DELETE FROM shift_rosters WHERE employee_id IN (9001, 9002);
DELETE FROM rotation_patterns WHERE id = 9001;
DELETE FROM shifts WHERE id IN (9001, 9002);
DELETE FROM employees WHERE id IN (9001, 9002);
DELETE FROM departments WHERE id = 9001;
