-- Test data for WFH integration tests
-- Uses IDs >= 9501 to avoid conflicts with existing seed/test data

INSERT INTO departments (id, name, code, active) VALUES (9501, 'WFH Test Dept', 'TSTDPT-WFH', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9501, 'EMP-WFH-001', 'Alice', 'WFH', 'alice.wfh@test.com', 9501);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id)
VALUES (9502, 'EMP-WFH-002', 'Bob', 'WFH', 'bob.wfh@test.com', 9501);
