DELETE FROM overtime_records WHERE employee_id IN (9001, 9002);
DELETE FROM overtime_policies WHERE id IN (9001, 9002);
DELETE FROM attendance_punches WHERE id IN (9001, 9002);
DELETE FROM devices WHERE id = 9001;
DELETE FROM shift_rosters WHERE id = 9001;
DELETE FROM shifts WHERE id = 9001;
DELETE FROM employees WHERE id IN (9001, 9002);
DELETE FROM departments WHERE id = 9001;
