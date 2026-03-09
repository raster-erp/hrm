-- =============================================================
-- V9: Seed comprehensive mock data for all Milestone 1 modules
-- =============================================================

-- Clean existing data (reverse FK order)
DELETE FROM no_dues;
DELETE FROM exit_checklists;
DELETE FROM separations;
DELETE FROM promotions;
DELETE FROM transfers;
DELETE FROM uniform_allocations;
DELETE FROM uniforms;
DELETE FROM id_cards;
DELETE FROM credential_attachments;
DELETE FROM credentials;
DELETE FROM contract_amendments;
DELETE FROM employment_contracts;
DELETE FROM employee_documents;
DELETE FROM employees;
DELETE FROM designations;
DELETE FROM departments;

-- ────────────────────────────────────────────
-- Departments (with hierarchy)
-- ────────────────────────────────────────────
INSERT INTO departments (id, name, code, parent_id, description, active) VALUES
(1, 'Executive', 'EXEC', NULL, 'Executive leadership team', TRUE),
(2, 'Human Resources', 'HR', 1, 'People management and administration', TRUE),
(3, 'Engineering', 'ENG', 1, 'Software development and engineering', TRUE),
(4, 'Finance', 'FIN', 1, 'Financial planning and accounting', TRUE),
(5, 'Marketing', 'MKT', 1, 'Marketing and communications', TRUE),
(6, 'Operations', 'OPS', 1, 'Business operations and logistics', TRUE),
(7, 'Quality Assurance', 'QA', 3, 'Testing and quality assurance', TRUE),
(8, 'DevOps', 'DEVOPS', 3, 'DevOps and infrastructure', TRUE),
(9, 'Recruitment', 'REC', 2, 'Talent acquisition and onboarding', TRUE),
(10, 'Payroll', 'PAY', 4, 'Salary processing and benefits', TRUE);

-- ────────────────────────────────────────────
-- Designations
-- ────────────────────────────────────────────
INSERT INTO designations (id, title, code, level, grade, department_id, description, active) VALUES
(1, 'CEO', 'CEO', 1, 'E1', 1, 'Chief Executive Officer', TRUE),
(2, 'CTO', 'CTO', 2, 'E2', 3, 'Chief Technology Officer', TRUE),
(3, 'HR Director', 'HRD', 2, 'E2', 2, 'Head of Human Resources', TRUE),
(4, 'Finance Director', 'FD', 2, 'E2', 4, 'Head of Finance', TRUE),
(5, 'Senior Engineer', 'SE', 3, 'M1', 3, 'Senior Software Engineer', TRUE),
(6, 'Software Engineer', 'SWE', 4, 'L3', 3, 'Software Engineer', TRUE),
(7, 'Junior Engineer', 'JE', 5, 'L2', 3, 'Junior Software Engineer', TRUE),
(8, 'QA Lead', 'QAL', 3, 'M1', 7, 'Quality Assurance Lead', TRUE),
(9, 'QA Engineer', 'QAE', 4, 'L3', 7, 'Quality Assurance Engineer', TRUE),
(10, 'HR Manager', 'HRM', 3, 'M1', 2, 'HR Manager', TRUE),
(11, 'HR Executive', 'HRE', 4, 'L3', 2, 'HR Executive', TRUE),
(12, 'Accountant', 'ACC', 4, 'L3', 4, 'Accountant', TRUE),
(13, 'Marketing Manager', 'MM', 3, 'M1', 5, 'Marketing Manager', TRUE),
(14, 'Marketing Executive', 'ME', 4, 'L3', 5, 'Marketing Executive', TRUE),
(15, 'DevOps Engineer', 'DOE', 4, 'L3', 8, 'DevOps Engineer', TRUE),
(16, 'Recruiter', 'RCR', 4, 'L3', 9, 'Talent Recruiter', TRUE),
(17, 'Operations Manager', 'OM', 3, 'M1', 6, 'Operations Manager', TRUE),
(18, 'Technical Lead', 'TL', 3, 'M1', 3, 'Technical Lead', TRUE);

-- ────────────────────────────────────────────
-- Employees (20 employees)
-- ────────────────────────────────────────────
INSERT INTO employees (id, employee_code, first_name, last_name, email, phone, date_of_birth, gender,
    address_line1, city, state, country, zip_code,
    emergency_contact_name, emergency_contact_phone, emergency_contact_relationship,
    bank_name, bank_account_number, bank_ifsc_code,
    department_id, designation_id, joining_date, employment_status) VALUES
(1, 'EMP001', 'James', 'Anderson', 'james.anderson@company.com', '+1-555-0101', '1975-03-15', 'MALE',
    '100 Executive Blvd', 'New York', 'NY', 'USA', '10001',
    'Mary Anderson', '+1-555-0102', 'Spouse',
    'Chase', '1234567890', 'CHAS0001',
    1, 1, '2018-01-15', 'ACTIVE'),
(2, 'EMP002', 'Sarah', 'Mitchell', 'sarah.mitchell@company.com', '+1-555-0201', '1980-07-22', 'FEMALE',
    '200 Tech Park', 'San Francisco', 'CA', 'USA', '94102',
    'John Mitchell', '+1-555-0202', 'Spouse',
    'Wells Fargo', '2345678901', 'WELL0001',
    3, 2, '2018-06-01', 'ACTIVE'),
(3, 'EMP003', 'Michael', 'Chen', 'michael.chen@company.com', '+1-555-0301', '1985-11-08', 'MALE',
    '300 HR Lane', 'Chicago', 'IL', 'USA', '60601',
    'Lisa Chen', '+1-555-0302', 'Spouse',
    'Bank of America', '3456789012', 'BOA00001',
    2, 3, '2019-02-15', 'ACTIVE'),
(4, 'EMP004', 'Emily', 'Roberts', 'emily.roberts@company.com', '+1-555-0401', '1988-04-12', 'FEMALE',
    '400 Finance St', 'Boston', 'MA', 'USA', '02101',
    'David Roberts', '+1-555-0402', 'Father',
    'Citibank', '4567890123', 'CITI0001',
    4, 4, '2019-05-01', 'ACTIVE'),
(5, 'EMP005', 'David', 'Kim', 'david.kim@company.com', '+1-555-0501', '1990-09-25', 'MALE',
    '500 Dev Avenue', 'Seattle', 'WA', 'USA', '98101',
    'Grace Kim', '+1-555-0502', 'Mother',
    'Chase', '5678901234', 'CHAS0002',
    3, 5, '2020-01-10', 'ACTIVE'),
(6, 'EMP006', 'Jessica', 'Patel', 'jessica.patel@company.com', '+1-555-0601', '1992-02-14', 'FEMALE',
    '600 Code Street', 'Austin', 'TX', 'USA', '73301',
    'Raj Patel', '+1-555-0602', 'Father',
    'Wells Fargo', '6789012345', 'WELL0002',
    3, 6, '2020-06-15', 'ACTIVE'),
(7, 'EMP007', 'Robert', 'Garcia', 'robert.garcia@company.com', '+1-555-0701', '1991-06-30', 'MALE',
    '700 Dev Lane', 'Denver', 'CO', 'USA', '80201',
    'Sofia Garcia', '+1-555-0702', 'Spouse',
    'Bank of America', '7890123456', 'BOA00002',
    3, 6, '2021-03-01', 'ACTIVE'),
(8, 'EMP008', 'Amanda', 'Taylor', 'amanda.taylor@company.com', '+1-555-0801', '1993-08-17', 'FEMALE',
    '800 Junior Dr', 'Portland', 'OR', 'USA', '97201',
    'Bruce Taylor', '+1-555-0802', 'Father',
    'Citibank', '8901234567', 'CITI0002',
    3, 7, '2022-01-10', 'ACTIVE'),
(9, 'EMP009', 'William', 'Brown', 'william.brown@company.com', '+1-555-0901', '1987-12-05', 'MALE',
    '900 QA Blvd', 'Atlanta', 'GA', 'USA', '30301',
    'Karen Brown', '+1-555-0902', 'Spouse',
    'Chase', '9012345678', 'CHAS0003',
    7, 8, '2019-08-15', 'ACTIVE'),
(10, 'EMP010', 'Laura', 'Martinez', 'laura.martinez@company.com', '+1-555-1001', '1994-05-20', 'FEMALE',
    '1000 QA Street', 'Miami', 'FL', 'USA', '33101',
    'Carlos Martinez', '+1-555-1002', 'Brother',
    'Wells Fargo', '0123456789', 'WELL0003',
    7, 9, '2021-04-01', 'ACTIVE'),
(11, 'EMP011', 'Daniel', 'Wilson', 'daniel.wilson@company.com', '+1-555-1101', '1989-10-11', 'MALE',
    '1100 HR Road', 'Minneapolis', 'MN', 'USA', '55401',
    'Jane Wilson', '+1-555-1102', 'Spouse',
    'Bank of America', '1122334455', 'BOA00003',
    2, 10, '2020-03-15', 'ACTIVE'),
(12, 'EMP012', 'Rachel', 'Lee', 'rachel.lee@company.com', '+1-555-1201', '1995-01-28', 'FEMALE',
    '1200 HR Circle', 'Phoenix', 'AZ', 'USA', '85001',
    'Tom Lee', '+1-555-1202', 'Father',
    'Citibank', '2233445566', 'CITI0003',
    2, 11, '2021-07-01', 'ACTIVE'),
(13, 'EMP013', 'Thomas', 'Nguyen', 'thomas.nguyen@company.com', '+1-555-1301', '1986-03-09', 'MALE',
    '1300 Finance Ave', 'Dallas', 'TX', 'USA', '75201',
    'Hoa Nguyen', '+1-555-1302', 'Mother',
    'Chase', '3344556677', 'CHAS0004',
    4, 12, '2019-11-01', 'ACTIVE'),
(14, 'EMP014', 'Stephanie', 'White', 'stephanie.white@company.com', '+1-555-1401', '1991-07-16', 'FEMALE',
    '1400 Marketing Dr', 'Los Angeles', 'CA', 'USA', '90001',
    'Roger White', '+1-555-1402', 'Spouse',
    'Wells Fargo', '4455667788', 'WELL0004',
    5, 13, '2020-09-01', 'ACTIVE'),
(15, 'EMP015', 'Christopher', 'Hall', 'christopher.hall@company.com', '+1-555-1501', '1993-11-03', 'MALE',
    '1500 Marketing Blvd', 'San Diego', 'CA', 'USA', '92101',
    'Nancy Hall', '+1-555-1502', 'Mother',
    'Bank of America', '5566778899', 'BOA00004',
    5, 14, '2021-05-15', 'ACTIVE'),
(16, 'EMP016', 'Nicole', 'Davis', 'nicole.davis@company.com', '+1-555-1601', '1990-04-27', 'FEMALE',
    '1600 DevOps Lane', 'Raleigh', 'NC', 'USA', '27601',
    'Mark Davis', '+1-555-1602', 'Spouse',
    'Citibank', '6677889900', 'CITI0004',
    8, 15, '2020-11-01', 'ACTIVE'),
(17, 'EMP017', 'Andrew', 'Jackson', 'andrew.jackson@company.com', '+1-555-1701', '1988-09-14', 'MALE',
    '1700 Recruit Rd', 'Nashville', 'TN', 'USA', '37201',
    'Patricia Jackson', '+1-555-1702', 'Spouse',
    'Chase', '7788990011', 'CHAS0005',
    9, 16, '2021-01-15', 'ACTIVE'),
(18, 'EMP018', 'Megan', 'Thompson', 'megan.thompson@company.com', '+1-555-1801', '1992-12-22', 'FEMALE',
    '1800 Ops Center', 'Indianapolis', 'IN', 'USA', '46201',
    'Paul Thompson', '+1-555-1802', 'Father',
    'Wells Fargo', '8899001122', 'WELL0005',
    6, 17, '2020-04-01', 'ACTIVE'),
(19, 'EMP019', 'Kevin', 'Lewis', 'kevin.lewis@company.com', '+1-555-1901', '1987-05-18', 'MALE',
    '1900 Tech Lead Way', 'Charlotte', 'NC', 'USA', '28201',
    'Susan Lewis', '+1-555-1902', 'Spouse',
    'Bank of America', '9900112233', 'BOA00005',
    3, 18, '2019-07-01', 'ACTIVE'),
(20, 'EMP020', 'Ashley', 'Clark', 'ashley.clark@company.com', '+1-555-2001', '1996-08-09', 'FEMALE',
    '2000 Payroll Plaza', 'Columbus', 'OH', 'USA', '43201',
    'Linda Clark', '+1-555-2002', 'Mother',
    'Citibank', '0011223344', 'CITI0005',
    10, 12, '2022-06-01', 'ACTIVE');

-- ────────────────────────────────────────────
-- Employment Contracts
-- ────────────────────────────────────────────
INSERT INTO employment_contracts (id, employee_id, contract_type, start_date, end_date, terms, status) VALUES
(1, 1, 'PERMANENT', '2018-01-15', NULL, 'Full-time executive contract with annual bonus and stock options.', 'ACTIVE'),
(2, 2, 'PERMANENT', '2018-06-01', NULL, 'Full-time CTO contract with performance-based equity vesting.', 'ACTIVE'),
(3, 3, 'PERMANENT', '2019-02-15', NULL, 'Full-time HR Director contract with standard benefits package.', 'ACTIVE'),
(4, 4, 'PERMANENT', '2019-05-01', NULL, 'Full-time Finance Director contract.', 'ACTIVE'),
(5, 5, 'PERMANENT', '2020-01-10', NULL, 'Full-time senior engineer contract with relocation benefits.', 'ACTIVE'),
(6, 6, 'PERMANENT', '2020-06-15', NULL, 'Full-time software engineer contract.', 'ACTIVE'),
(7, 7, 'PERMANENT', '2021-03-01', NULL, 'Full-time software engineer contract with signing bonus.', 'ACTIVE'),
(8, 8, 'CONTRACT', '2022-01-10', '2024-12-31', '2-year contract with option for extension. Standard hourly rate.', 'ACTIVE'),
(9, 9, 'PERMANENT', '2019-08-15', NULL, 'Full-time QA Lead contract.', 'ACTIVE'),
(10, 10, 'CONTRACT', '2021-04-01', '2025-03-31', '4-year contract. Performance review every 6 months.', 'ACTIVE'),
(11, 11, 'PERMANENT', '2020-03-15', NULL, 'Full-time HR Manager contract.', 'ACTIVE'),
(12, 12, 'PERMANENT', '2021-07-01', NULL, 'Full-time HR Executive contract.', 'ACTIVE'),
(13, 13, 'PERMANENT', '2019-11-01', NULL, 'Full-time Accountant contract.', 'ACTIVE'),
(14, 14, 'PERMANENT', '2020-09-01', NULL, 'Full-time Marketing Manager contract.', 'ACTIVE'),
(15, 15, 'PROBATION', '2021-05-15', '2021-11-15', '6-month probation period.', 'TERMINATED'),
(16, 15, 'PERMANENT', '2021-11-16', NULL, 'Converted to permanent after successful probation.', 'ACTIVE'),
(17, 16, 'PERMANENT', '2020-11-01', NULL, 'Full-time DevOps Engineer contract.', 'ACTIVE'),
(18, 17, 'PERMANENT', '2021-01-15', NULL, 'Full-time Recruiter contract.', 'ACTIVE'),
(19, 18, 'PERMANENT', '2020-04-01', NULL, 'Full-time Operations Manager contract.', 'ACTIVE'),
(20, 19, 'PERMANENT', '2019-07-01', NULL, 'Full-time Technical Lead contract.', 'ACTIVE'),
(21, 20, 'PROBATION', '2022-06-01', '2022-12-01', '6-month probation for Payroll department.', 'TERMINATED'),
(22, 20, 'PERMANENT', '2022-12-02', NULL, 'Permanent contract after probation.', 'ACTIVE');

-- Contract amendments
INSERT INTO contract_amendments (id, contract_id, amendment_date, description, old_terms, new_terms) VALUES
(1, 1, '2020-01-01', 'Annual compensation revision', 'Base salary: $200,000', 'Base salary: $225,000 + 15% annual bonus'),
(2, 2, '2020-06-01', 'Equity vesting update', 'Standard 4-year vest', '4-year vest with 1-year cliff + additional RSU grant'),
(3, 5, '2022-01-01', 'Promotion-related salary adjustment', 'Base salary: $120,000', 'Base salary: $145,000 + tech lead allowance'),
(4, 6, '2023-06-01', 'Annual review adjustment', 'Base salary: $95,000', 'Base salary: $110,000');

-- ────────────────────────────────────────────
-- Credentials
-- ────────────────────────────────────────────
INSERT INTO credentials (id, employee_id, credential_type, credential_name, issuer, issue_date, expiry_date, credential_number, verification_status, notes) VALUES
(1, 5, 'CERTIFICATION', 'AWS Solutions Architect', 'Amazon Web Services', '2022-03-15', '2025-03-15', 'AWS-SAP-2022-0501', 'VERIFIED', 'Professional level certification'),
(2, 5, 'CERTIFICATION', 'Kubernetes Administrator', 'CNCF', '2023-01-20', '2026-01-20', 'CKA-2023-0502', 'VERIFIED', NULL),
(3, 6, 'CERTIFICATION', 'Oracle Java SE 17', 'Oracle', '2022-08-10', '2025-08-10', 'OJP-2022-0601', 'VERIFIED', 'Java professional developer certification'),
(4, 7, 'CERTIFICATION', 'Spring Professional', 'VMware', '2023-05-22', '2026-05-22', 'VSP-2023-0701', 'VERIFIED', NULL),
(5, 9, 'CERTIFICATION', 'ISTQB Foundation', 'ISTQB', '2020-06-15', NULL, 'ISTQB-FL-0901', 'VERIFIED', 'Foundation level - no expiry'),
(6, 16, 'CERTIFICATION', 'AWS DevOps Engineer', 'Amazon Web Services', '2021-09-10', '2024-09-10', 'AWS-DOP-2021-1601', 'EXPIRED', 'Needs renewal'),
(7, 19, 'CERTIFICATION', 'PMP', 'PMI', '2020-11-01', '2023-11-01', 'PMP-2020-1901', 'EXPIRED', 'Expired - renewal in progress'),
(8, 2, 'DEGREE', 'PhD Computer Science', 'Stanford University', '2010-06-15', NULL, 'STAN-PHD-2010', 'VERIFIED', NULL),
(9, 4, 'DEGREE', 'MBA Finance', 'Wharton School', '2015-05-20', NULL, 'WHAR-MBA-2015', 'VERIFIED', NULL),
(10, 8, 'CERTIFICATION', 'React Developer', 'Meta', '2023-03-01', '2026-03-01', 'META-RD-2023-0801', 'PENDING', 'Awaiting verification'),
(11, 13, 'CERTIFICATION', 'CPA', 'AICPA', '2018-04-15', NULL, 'CPA-2018-1301', 'VERIFIED', 'Certified Public Accountant'),
(12, 14, 'CERTIFICATION', 'Google Analytics', 'Google', '2022-07-01', '2025-07-01', 'GA-2022-1401', 'VERIFIED', NULL);

-- ────────────────────────────────────────────
-- ID Cards
-- ────────────────────────────────────────────
INSERT INTO id_cards (id, employee_id, card_number, issue_date, expiry_date, status) VALUES
(1, 1, 'IDC-2018-001', '2018-01-20', '2026-01-20', 'ACTIVE'),
(2, 2, 'IDC-2018-002', '2018-06-10', '2026-06-10', 'ACTIVE'),
(3, 3, 'IDC-2019-003', '2019-02-20', '2027-02-20', 'ACTIVE'),
(4, 4, 'IDC-2019-004', '2019-05-10', '2027-05-10', 'ACTIVE'),
(5, 5, 'IDC-2020-005', '2020-01-15', '2028-01-15', 'ACTIVE'),
(6, 6, 'IDC-2020-006', '2020-06-20', '2028-06-20', 'ACTIVE'),
(7, 7, 'IDC-2021-007', '2021-03-05', '2029-03-05', 'ACTIVE'),
(8, 8, 'IDC-2022-008', '2022-01-15', '2024-12-31', 'ACTIVE'),
(9, 9, 'IDC-2019-009', '2019-08-20', '2027-08-20', 'ACTIVE'),
(10, 10, 'IDC-2021-010', '2021-04-10', '2025-03-31', 'ACTIVE'),
(11, 11, 'IDC-2020-011', '2020-03-20', '2028-03-20', 'ACTIVE'),
(12, 12, 'IDC-2021-012', '2021-07-10', '2029-07-10', 'ACTIVE'),
(13, 13, 'IDC-2019-013', '2019-11-10', '2027-11-10', 'ACTIVE'),
(14, 14, 'IDC-2020-014', '2020-09-10', '2028-09-10', 'ACTIVE'),
(15, 15, 'IDC-2021-015', '2021-05-20', '2029-05-20', 'ACTIVE'),
(16, 16, 'IDC-2020-016', '2020-11-10', '2028-11-10', 'ACTIVE'),
(17, 17, 'IDC-2021-017', '2021-01-20', '2029-01-20', 'ACTIVE'),
(18, 18, 'IDC-2020-018', '2020-04-10', '2028-04-10', 'ACTIVE'),
(19, 19, 'IDC-2019-019', '2019-07-10', '2027-07-10', 'ACTIVE'),
(20, 20, 'IDC-2022-020', '2022-06-10', '2024-06-10', 'EXPIRED');

-- ────────────────────────────────────────────
-- Uniforms
-- ────────────────────────────────────────────
INSERT INTO uniforms (id, name, type, size, description, active) VALUES
(1, 'Corporate Blazer - Navy', 'COAT', 'L', 'Navy blue corporate blazer', TRUE),
(2, 'Corporate Blazer - Navy', 'COAT', 'M', 'Navy blue corporate blazer - Medium', TRUE),
(3, 'Dress Shirt - White', 'SHIRT', 'L', 'White formal dress shirt', TRUE),
(4, 'Dress Shirt - White', 'SHIRT', 'M', 'White formal dress shirt - Medium', TRUE),
(5, 'Dress Shirt - Blue', 'SHIRT', 'L', 'Light blue formal dress shirt', TRUE),
(6, 'Formal Trousers - Navy', 'PANTS', 'L', 'Navy formal trousers', TRUE),
(7, 'Formal Trousers - Navy', 'PANTS', 'M', 'Navy formal trousers - Medium', TRUE),
(8, 'Safety Vest', 'VEST', 'XL', 'High-visibility safety vest for ops team', TRUE),
(9, 'Polo Shirt - Company Logo', 'SHIRT', 'L', 'Casual polo with company branding', TRUE),
(10, 'Polo Shirt - Company Logo', 'SHIRT', 'M', 'Casual polo with company branding - Medium', TRUE);

-- ────────────────────────────────────────────
-- Uniform Allocations
-- ────────────────────────────────────────────
INSERT INTO uniform_allocations (id, employee_id, uniform_id, allocated_date, returned_date, status) VALUES
(1, 1, 1, '2018-02-01', NULL, 'ALLOCATED'),
(2, 1, 3, '2018-02-01', NULL, 'ALLOCATED'),
(3, 1, 6, '2018-02-01', NULL, 'ALLOCATED'),
(4, 2, 2, '2018-06-15', NULL, 'ALLOCATED'),
(5, 2, 4, '2018-06-15', NULL, 'ALLOCATED'),
(6, 3, 2, '2019-03-01', NULL, 'ALLOCATED'),
(7, 3, 5, '2019-03-01', NULL, 'ALLOCATED'),
(8, 5, 1, '2020-02-01', NULL, 'ALLOCATED'),
(9, 5, 9, '2020-02-01', NULL, 'ALLOCATED'),
(10, 6, 10, '2020-07-01', NULL, 'ALLOCATED'),
(11, 7, 9, '2021-03-15', NULL, 'ALLOCATED'),
(12, 18, 8, '2020-04-15', NULL, 'ALLOCATED'),
(13, 8, 10, '2022-02-01', '2022-12-31', 'RETURNED'),
(14, 8, 9, '2023-01-10', NULL, 'ALLOCATED');

-- ────────────────────────────────────────────
-- Transfers
-- ────────────────────────────────────────────
INSERT INTO transfers (id, employee_id, from_department_id, to_department_id, from_branch, to_branch, transfer_type, effective_date, status, reason, approved_by, approved_at) VALUES
(1, 7, 3, 7, NULL, NULL, 'INTER_DEPARTMENT', '2023-06-01', 'EXECUTED', 'Moving to QA team for cross-functional experience', 1, '2023-05-15 10:00:00'),
(2, 12, 2, 9, NULL, NULL, 'INTER_DEPARTMENT', '2024-01-15', 'APPROVED', 'Transition to recruitment team', 3, '2024-01-05 14:30:00'),
(3, 15, 5, 6, 'West Coast', 'Central', 'INTER_BRANCH', '2024-03-01', 'PENDING', 'Branch consolidation - marketing to operations support', NULL, NULL),
(4, 10, 7, 8, NULL, NULL, 'INTER_DEPARTMENT', '2023-09-01', 'EXECUTED', 'QA to DevOps transition for automation focus', 2, '2023-08-20 09:00:00'),
(5, 6, 3, 3, 'Austin Office', 'Seattle Office', 'INTER_BRANCH', '2024-06-01', 'PENDING', 'Relocation request for personal reasons', NULL, NULL);

-- ────────────────────────────────────────────
-- Promotions
-- ────────────────────────────────────────────
INSERT INTO promotions (id, employee_id, old_designation_id, new_designation_id, old_grade, new_grade, effective_date, status, reason, approved_by, approved_at) VALUES
(1, 5, 6, 5, 'L3', 'M1', '2022-01-01', 'EXECUTED', 'Excellent performance, 2 years as SWE. Ready for senior role.', 2, '2021-12-15 10:00:00'),
(2, 6, 7, 6, 'L2', 'L3', '2023-07-01', 'EXECUTED', 'Strong contributions to core platform. Promoted to SWE.', 2, '2023-06-20 14:00:00'),
(3, 11, 11, 10, 'L3', 'M1', '2023-04-01', 'EXECUTED', 'Promoted to HR Manager after demonstrating leadership.', 3, '2023-03-15 11:00:00'),
(4, 8, 7, 6, 'L2', 'L3', '2024-04-01', 'APPROVED', 'Contract renewal with promotion to Software Engineer.', 2, '2024-03-20 09:00:00'),
(5, 19, 5, 18, 'M1', 'M1', '2024-07-01', 'PENDING', 'Nomination for Technical Lead role.', NULL, NULL),
(6, 15, 14, 13, 'L3', 'M1', '2024-09-01', 'PENDING', 'Promotion to Marketing Manager.', NULL, NULL);

-- ────────────────────────────────────────────
-- Separations
-- ────────────────────────────────────────────
INSERT INTO separations (id, employee_id, separation_type, reason, notice_date, last_working_day, status, approved_by, approved_at) VALUES
(1, 20, 'RESIGNATION', 'Pursuing higher education abroad.', '2024-06-01', '2024-07-01', 'APPROVED', 3, '2024-06-05 10:00:00'),
(2, 8, 'END_OF_CONTRACT', 'Contract period ending on 2024-12-31.', '2024-10-01', '2024-12-31', 'PENDING', NULL, NULL);

-- ────────────────────────────────────────────
-- Exit Checklists (for separation id=1)
-- ────────────────────────────────────────────
INSERT INTO exit_checklists (id, separation_id, item_name, department, is_cleared, cleared_by, cleared_at, notes) VALUES
(1, 1, 'Return laptop and peripherals', 'IT', TRUE, 'IT Admin', '2024-06-20 15:00:00', 'MacBook Pro returned in good condition'),
(2, 1, 'Return ID card and access badge', 'Security', TRUE, 'Security Team', '2024-06-25 10:00:00', NULL),
(3, 1, 'Knowledge transfer documentation', 'Payroll', FALSE, NULL, NULL, 'Pending handover documents'),
(4, 1, 'Return parking pass', 'Facilities', TRUE, 'Facility Manager', '2024-06-22 09:00:00', NULL),
(5, 1, 'Email and system access revocation', 'IT', FALSE, NULL, NULL, 'Scheduled for last working day');

-- ────────────────────────────────────────────
-- No Dues (for separation id=1)
-- ────────────────────────────────────────────
INSERT INTO no_dues (id, separation_id, department, is_cleared, cleared_by, cleared_at, amount_due, notes) VALUES
(1, 1, 'Finance', TRUE, 'Finance Team', '2024-06-18 11:00:00', 0.00, 'No outstanding advances'),
(2, 1, 'IT', FALSE, NULL, NULL, 250.00, 'Pending laptop insurance deduction'),
(3, 1, 'HR', TRUE, 'HR Team', '2024-06-19 14:00:00', 0.00, 'Leave encashment processed'),
(4, 1, 'Library', TRUE, 'Library Admin', '2024-06-17 10:00:00', 0.00, 'All books returned');
