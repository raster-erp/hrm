-- Test data for CompOff integration tests
-- Uses IDs >= 9900 to avoid conflicts with other test data

INSERT INTO departments (id, name, code, active) VALUES (9900, 'Test Dept CompOff', 'TSTCO', true);

INSERT INTO employees (id, employee_code, first_name, last_name, email, department_id, basic_salary)
VALUES (9901, 'CO-EMP-001', 'Jane', 'CompOff', 'jane.compoff@test.com', 9900, 25000.00);
