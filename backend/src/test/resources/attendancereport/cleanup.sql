DELETE FROM report_schedules WHERE department_id = 9701;
DELETE FROM attendance_punches WHERE id IN (9701, 9702);
DELETE FROM devices WHERE id = 9701;
DELETE FROM employees WHERE id IN (9701, 9702);
DELETE FROM departments WHERE id = 9701;
