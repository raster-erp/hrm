DELETE FROM wfh_activity_logs WHERE wfh_request_id IN (SELECT id FROM wfh_requests WHERE employee_id IN (9501, 9502));
DELETE FROM wfh_requests WHERE employee_id IN (9501, 9502);
DELETE FROM employees WHERE id IN (9501, 9502);
DELETE FROM departments WHERE id = 9501;
