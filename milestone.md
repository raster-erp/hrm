# HRM – Project Milestones

---

## Technical Stack. 

| Layer | Technology | Details |
|-------|-----------|---------|
| Backend Framework | Spring Boot | REST API development with Spring Boot |
| Language | Java 21 | LTS release with virtual threads, pattern matching, and record patterns |
| Frontend Framework | Angular | Single-page application with TypeScript |
| Build & Dependency Management | Gradle | Build automation and library management |
| Production Database | PostgreSQL | Primary relational database for all environments |
| Development Database | H2 | In-memory database for local development and testing |
| Database Versioning | Flyway | Schema migration and version control for database changes |

---

## Milestone 1: Employee Management

### 1.1 Employee Master & Profile

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Gather employee data fields (personal, contact, emergency, bank details) | Done |
| Requirements | Define role-based access rules for profile viewing and editing | Not Started |
| Database Design | Design `employees` table with all demographic and employment columns | Done |
| Database Design | Design `employee_documents` table for uploaded files (photo, ID proof, etc.) | Done |
| Database Design | Create indexes on employee code, department, and status columns | Done |
| Backend Development | Build CRUD APIs for employee creation, update, retrieval, and soft-delete | Done |
| Backend Development | Implement file-upload API for employee photo and documents | Not Started |
| Backend Development | Add search and filter API (by name, department, status, joining date range) | Done |
| Backend Development | Implement bulk-import API (CSV/Excel) for employee master data | Not Started |
| Frontend Development | Build employee list page with pagination, search, and filter controls | Done |
| Frontend Development | Build employee creation/edit form with field validations | Done |
| Frontend Development | Build employee profile detail view with tabbed sections | Done |
| Frontend Development | Implement profile photo upload with preview and crop | Not Started |
| Integration Testing | Validate end-to-end CRUD operations for employee records | Not Started |
| Integration Testing | Verify file upload, storage, and retrieval workflows | Not Started |
| UAT & QA | Conduct user acceptance testing with sample employee data | Not Started |
| Deployment | Deploy employee master module to staging and production | Not Started |

### 1.2 Department & Designation Setup

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define department hierarchy model (parent-child departments) | Done |
| Requirements | Define designation levels and mapping to pay grades | Done |
| Database Design | Design `departments` table with self-referencing parent column for hierarchy | Done |
| Database Design | Design `designations` table with level, grade, and department linkage | Done |
| Backend Development | Build CRUD APIs for departments with hierarchy support | Done |
| Backend Development | Build CRUD APIs for designations with grade linkage | Done |
| Backend Development | Add validation to prevent deletion of departments/designations in use | Done |
| Frontend Development | Build department tree view with drag-and-drop reordering | Done |
| Frontend Development | Build designation list and form with grade selection | Done |
| Integration Testing | Validate hierarchy creation, reordering, and deletion constraints | Not Started |
| UAT & QA | Conduct user acceptance testing for department and designation setup | Not Started |
| Deployment | Deploy department and designation module to staging and production | Not Started |

### 1.3 Employment Contract Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define contract types (permanent, probation, fixed-term, consultant) | Done |
| Requirements | Define contract lifecycle states and renewal rules | Done |
| Database Design | Design `employment_contracts` table with type, start/end dates, and terms | Done |
| Database Design | Design `contract_amendments` table for tracking modifications | Done |
| Backend Development | Build CRUD APIs for contract creation, amendment, and renewal | Done |
| Backend Development | Implement automated alerts for contracts approaching expiry | Done |
| Backend Development | Build contract PDF generation API with template support | Not Started |
| Frontend Development | Build contract list view with status filters (active, expired, renewed) | Done |
| Frontend Development | Build contract form with clause editor and digital signature placeholder | Done |
| Frontend Development | Add contract timeline visualization for each employee | Done |
| Integration Testing | Validate contract lifecycle transitions and renewal workflows | Not Started |
| UAT & QA | Conduct user acceptance testing for contract management flows | Not Started |
| Deployment | Deploy contract management module to staging and production | Not Started |

### 1.4 Credential & License Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Identify credential/license types (medical license, certifications, etc.) | Done |
| Requirements | Define expiry notification rules and renewal workflows | Done |
| Database Design | Design `credentials` table with type, issue date, expiry date, and issuer | Done |
| Database Design | Design `credential_attachments` table for uploaded certificates | Done |
| Backend Development | Build CRUD APIs for credential records with file attachments | Done |
| Backend Development | Implement scheduled job for expiry notifications (email/SMS) | Not Started |
| Backend Development | Build credential verification status API (verified, pending, expired) | Done |
| Frontend Development | Build credential list and detail view per employee | Done |
| Frontend Development | Build credential form with document upload and expiry date picker | Done |
| Frontend Development | Add dashboard widget for credentials expiring within 30/60/90 days | Done |
| Integration Testing | Validate expiry notification triggers and credential lifecycle | Not Started |
| UAT & QA | Conduct user acceptance testing for credential tracking | Not Started |
| Deployment | Deploy credential tracking module to staging and production | Not Started |

### 1.5 ID Card & Uniform Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define ID card template fields and layout options | Not Started |
| Requirements | Define uniform catalog (sizes, types) and allocation rules | Not Started |
| Database Design | Design `id_cards` table with card number, issue date, and status | Done |
| Database Design | Design `uniforms` and `uniform_allocations` tables | Done |
| Backend Development | Build ID card generation API with photo, barcode/QR code support | Done |
| Backend Development | Build uniform allocation and return tracking APIs | Done |
| Backend Development | Add bulk ID card printing support (batch PDF generation) | Not Started |
| Frontend Development | Build ID card preview and print interface | Not Started |
| Frontend Development | Build uniform catalog management and allocation form | Not Started |
| Integration Testing | Validate ID card generation, printing, and uniform allocation flows | Not Started |
| UAT & QA | Conduct user acceptance testing for ID card and uniform modules | Not Started |
| Deployment | Deploy ID card and uniform module to staging and production | Not Started |

### 1.6 Employee Transfer & Promotion

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define transfer types (inter-department, inter-branch, inter-company) | Not Started |
| Requirements | Define promotion criteria and approval workflow rules | Not Started |
| Database Design | Design `transfers` table with from/to department, branch, and effective date | Done |
| Database Design | Design `promotions` table with old/new designation, grade, and effective date | Done |
| Backend Development | Build transfer request, approval, and execution APIs | Done |
| Backend Development | Build promotion initiation, approval, and execution APIs | Done |
| Backend Development | Implement automatic update of employee record upon approval | Done |
| Backend Development | Build transfer/promotion history API for audit trail | Done |
| Frontend Development | Build transfer request form with branch/department selectors | Not Started |
| Frontend Development | Build promotion form with designation and grade selectors | Not Started |
| Frontend Development | Build approval workflow UI with multi-level approver support | Not Started |
| Frontend Development | Add transfer/promotion history timeline on employee profile | Not Started |
| Integration Testing | Validate transfer and promotion workflows end-to-end | Not Started |
| UAT & QA | Conduct user acceptance testing for transfer and promotion flows | Not Started |
| Deployment | Deploy transfer and promotion module to staging and production | Not Started |

### 1.7 Exit & Separation Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define separation types (resignation, termination, retirement, absconding) | Not Started |
| Requirements | Define exit checklist items and no-dues clearance process | Not Started |
| Database Design | Design `separations` table with type, notice period, and last working day | Done |
| Database Design | Design `exit_checklists` and `no_dues` tables | Done |
| Backend Development | Build separation initiation and approval APIs | Done |
| Backend Development | Build exit checklist and no-dues clearance APIs | Done |
| Backend Development | Implement full-and-final settlement computation API | Not Started |
| Backend Development | Build exit interview feedback capture API | Not Started |
| Frontend Development | Build separation request form with reason and notice period fields | Not Started |
| Frontend Development | Build exit checklist dashboard with department-wise clearance status | Not Started |
| Frontend Development | Build full-and-final settlement summary view | Not Started |
| Integration Testing | Validate end-to-end separation workflow including clearance | Not Started |
| UAT & QA | Conduct user acceptance testing for exit and separation flows | Not Started |
| Deployment | Deploy exit and separation module to staging and production | Not Started |

---

## Milestone 2: Attendance & Shift Management

### 2.1 Biometric / RFID Integration

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Identify supported biometric/RFID device models and protocols | Done |
| Requirements | Define punch data format and sync frequency requirements | Done |
| Database Design | Design `attendance_punches` table with device ID, punch time, and direction | Done |
| Database Design | Design `devices` table for registered biometric/RFID terminals | Done |
| Backend Development | Build device registration and configuration APIs | Done |
| Backend Development | Implement device data sync service (push/pull protocols) | Done |
| Backend Development | Build raw punch data ingestion and normalization API | Done |
| Backend Development | Add duplicate punch detection and filtering logic | Done |
| Frontend Development | Build device management dashboard (status, last sync, connectivity) | Done |
| Frontend Development | Build raw punch log viewer with date and employee filters | Done |
| Integration Testing | Test with physical devices for punch data capture and sync | Not Started |
| Integration Testing | Validate duplicate detection and data normalization | Done |
| UAT & QA | Conduct field testing at multiple locations with live devices | Not Started |
| Deployment | Deploy biometric integration service to staging and production | Not Started |

### 2.2 Shift Roster Planning

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define shift types (general, morning, evening, night, rotational, split) | Done |
| Requirements | Define roster assignment rules and rotation patterns | Done |
| Database Design | Design `shifts` table with start time, end time, break duration, and grace period | Done |
| Database Design | Design `shift_rosters` table with employee, shift, and effective date range | Done |
| Database Design | Design `rotation_patterns` table for automated rotation scheduling | Done |
| Backend Development | Build CRUD APIs for shift master configuration | Done |
| Backend Development | Build roster assignment API (individual and bulk) | Done |
| Backend Development | Implement auto-rotation engine based on defined patterns | Done |
| Backend Development | Add conflict detection (overlapping shifts, insufficient rest gap) | Done |
| Frontend Development | Build shift master setup form with time pickers and grace settings | Done |
| Frontend Development | Build roster calendar view with drag-and-drop assignment | Done |
| Frontend Development | Build bulk roster assignment wizard (by department, team, or group) | Done |
| Integration Testing | Validate roster assignment, rotation, and conflict detection | Done |
| UAT & QA | Conduct user acceptance testing for shift roster planning | Not Started |
| Deployment | Deploy shift roster module to staging and production | Not Started |

### 2.3 Overtime Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define overtime eligibility rules and approval workflows | Done |
| Requirements | Define overtime rate slabs (weekday, weekend, holiday, double-time) | Done |
| Database Design | Design `overtime_policies` table with rate multipliers and caps | Done |
| Database Design | Design `overtime_records` table with employee, hours, type, and approval status | Done |
| Backend Development | Build overtime policy configuration APIs | Done |
| Backend Development | Build overtime request, calculation, and approval APIs | Done |
| Backend Development | Implement auto-detection of overtime from attendance punch data | Done |
| Backend Development | Build overtime summary API for payroll integration | Done |
| Frontend Development | Build overtime policy setup form with rate slab configuration | Done |
| Frontend Development | Build overtime request and approval interface | Done |
| Frontend Development | Build overtime summary dashboard with charts and drill-down | Done |
| Integration Testing | Validate overtime calculation against multiple rate slabs | Done |
| Integration Testing | Verify overtime data flows correctly into payroll computation | Done |
| UAT & QA | Conduct user acceptance testing for overtime management | Not Started |
| Deployment | Deploy overtime management module to staging and production | Not Started |

### 2.4 Late Coming & Early Going Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define late-coming and early-going thresholds and grace periods | Done |
| Requirements | Define penalty rules (warning, leave deduction, pay cut) | Done |
| Database Design | Design `attendance_deviations` table with type, minutes, and penalty applied | Done |
| Backend Development | Build deviation detection engine (compares punch times against shift) | Done |
| Backend Development | Build penalty computation and auto-application APIs | Done |
| Backend Development | Implement monthly deviation summary API per employee | Done |
| Frontend Development | Build deviation report view with filters (date range, department, employee) | Done |
| Frontend Development | Build penalty configuration form with threshold and rule settings | Done |
| Integration Testing | Validate deviation detection accuracy across shift types | Done |
| UAT & QA | Conduct user acceptance testing for deviation tracking | Not Started |
| Deployment | Deploy late/early tracking module to staging and production | Not Started |

### 2.5 Attendance Regularization

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define regularization request types (missed punch, on-duty, client visit) | Done |
| Requirements | Define approval hierarchy and auto-approval rules | Done |
| Database Design | Design `regularization_requests` table with type, reason, and corrected times | Done |
| Backend Development | Build regularization request submission API | Done |
| Backend Development | Build multi-level approval workflow API | Done |
| Backend Development | Implement auto-update of attendance records upon approval | Done |
| Frontend Development | Build regularization request form with reason dropdown and time correction | Done |
| Frontend Development | Build approval inbox with bulk approve/reject capability | Done |
| Frontend Development | Show regularization history on employee attendance detail page | Done |
| Integration Testing | Validate regularization workflow from request to attendance update | Done |
| UAT & QA | Conduct user acceptance testing for attendance regularization | Not Started |
| Deployment | Deploy regularization module to staging and production | Not Started |

### 2.6 Work-From-Home Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define WFH eligibility criteria and policy rules | Done |
| Requirements | Define WFH request and check-in/check-out workflow | Done |
| Database Design | Design `wfh_requests` table with date, reason, and approval status | Done |
| Database Design | Design `wfh_activity_logs` table for check-in/check-out tracking | Done |
| Backend Development | Build WFH request and approval APIs | Done |
| Backend Development | Build WFH check-in/check-out API with IP/location validation | Done |
| Backend Development | Implement WFH attendance integration (mark as present-WFH) | Done |
| Frontend Development | Build WFH request form with date picker and reason field | Done |
| Frontend Development | Build WFH check-in/check-out interface with status indicator | Done |
| Frontend Development | Build WFH dashboard showing team WFH distribution | Done |
| Integration Testing | Validate WFH request, approval, and attendance marking | Done |
| UAT & QA | Conduct user acceptance testing for WFH tracking | Not Started |
| Deployment | Deploy WFH tracking module to staging and production | Not Started |

### 2.7 Attendance Reports

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define report types (daily muster, monthly summary, absentee list) | Done |
| Requirements | Define export formats (PDF, Excel, CSV) and scheduling requirements | Done |
| Database Design | Design `report_schedules` table for automated report generation | Done |
| Backend Development | Build daily attendance muster report API | Done |
| Backend Development | Build monthly attendance summary API with present/absent/leave counts | Done |
| Backend Development | Build absentee and defaulter list report API | Done |
| Backend Development | Implement report export service (PDF, Excel, CSV generators) | Done |
| Backend Development | Add scheduled report generation and email delivery service | Done |
| Frontend Development | Build report selection interface with date range and department filters | Done |
| Frontend Development | Build interactive report viewer with drill-down capability | Done |
| Frontend Development | Add report download and email-share options | Done |
| Integration Testing | Validate report accuracy against raw attendance data | Done |
| UAT & QA | Conduct user acceptance testing for all attendance reports | Not Started |
| Deployment | Deploy attendance reports module to staging and production | Not Started |

---

## Milestone 3: Leave Management

### 3.1 Leave Type & Policy Setup

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define leave types (casual, sick, earned, maternity, paternity, unpaid, etc.) | Done |
| Requirements | Define accrual rules (monthly, quarterly, annual, pro-rata for new joiners) | Done |
| Requirements | Define carry-forward, lapsing, and maximum accumulation rules | Done |
| Database Design | Design `leave_types` table with code, name, and category | Done |
| Database Design | Design `leave_policies` table with accrual, carry-forward, and eligibility rules | Done |
| Database Design | Design `leave_policy_assignments` table for linking policies to employee groups | Done |
| Backend Development | Build CRUD APIs for leave type master | Done |
| Backend Development | Build CRUD APIs for leave policy configuration | Done |
| Backend Development | Build policy assignment API (by grade, department, or individual) | Done |
| Backend Development | Implement accrual engine (scheduled job for periodic credit) | Done |
| Frontend Development | Build leave type master list and form | Done |
| Frontend Development | Build leave policy configuration wizard with accrual rule builder | Done |
| Frontend Development | Build policy assignment interface with employee group selector | Done |
| Integration Testing | Validate accrual computations across different policy configurations | Done |
| UAT & QA | Conduct user acceptance testing for leave type and policy setup | Done |
| Deployment | Deploy leave policy module to staging and production | Not Started |

### 3.2 Leave Application & Approval

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define application rules (advance notice, max duration, document requirements) | Done |
| Requirements | Define approval hierarchy (reporting manager, HR, skip-level) | Done |
| Database Design | Design `leave_applications` table with type, dates, reason, and approval chain | Done |
| Database Design | Design `leave_approval_logs` table for tracking approver actions | Done |
| Backend Development | Build leave application submission API with balance validation | Done |
| Backend Development | Build multi-level approval workflow API | Done |
| Backend Development | Implement leave cancellation and revocation APIs | Done |
| Backend Development | Add email/push notification triggers for application lifecycle events | Done |
| Frontend Development | Build leave application form with type selector, date range picker, and reason | Done |
| Frontend Development | Build approval inbox with leave details, balance context, and team calendar | Done |
| Frontend Development | Build leave status tracker showing application progress | Done |
| Integration Testing | Validate full application-to-approval lifecycle | Done |
| Integration Testing | Verify balance deduction and restoration on cancellation | Done |
| UAT & QA | Conduct user acceptance testing for leave application and approval | Not Started |
| Deployment | Deploy leave application module to staging and production | Not Started |

### 3.3 Leave Balance Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define balance computation rules (credited, used, pending, available) | Done |
| Requirements | Define year-end processing rules (carry-forward, lapsing) | Done |
| Database Design | Design `leave_balances` table with type-wise credit, used, and available columns | Done |
| Database Design | Design `leave_transactions` table for detailed audit of every balance change | Done |
| Backend Development | Build leave balance inquiry API (current balance by type) | Done |
| Backend Development | Build leave transaction history API with filters | Done |
| Backend Development | Implement year-end processing job (carry-forward and lapsing) | Done |
| Backend Development | Build balance adjustment API for manual corrections by HR | Done |
| Frontend Development | Build leave balance dashboard showing type-wise summary | Done |
| Frontend Development | Build leave transaction history view with filters | Done |
| Frontend Development | Build year-end processing trigger and review interface for HR | Done |
| Integration Testing | Validate balance updates across application, cancellation, and accrual | Done |
| UAT & QA | Conduct user acceptance testing for leave balance tracking | Not Started |
| Deployment | Deploy leave balance module to staging and production | Not Started |

### 3.4 Leave Encashment Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define encashment eligibility rules (minimum balance, leave types eligible) | Done |
| Requirements | Define encashment calculation formula (basic pay × days) | Done |
| Database Design | Design `leave_encashments` table with days, amount, and approval status | Done |
| Backend Development | Build encashment eligibility check API | Done |
| Backend Development | Build encashment request and approval APIs | Done |
| Backend Development | Implement encashment calculation engine linked to salary data | Done |
| Backend Development | Build encashment payout integration with payroll | Done |
| Frontend Development | Build encashment request form with eligible days and computed amount preview | Done |
| Frontend Development | Build encashment approval interface for HR/finance | Done |
| Integration Testing | Validate encashment calculation and payroll integration | Done |
| UAT & QA | Conduct user acceptance testing for leave encashment | Not Started |
| Deployment | Deploy leave encashment module to staging and production | Not Started |

### 3.5 Comp-Off Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define comp-off eligibility (worked on holiday/weekend/extra shift) | Done |
| Requirements | Define comp-off expiry rules and usage restrictions | Done |
| Database Design | Design `comp_off_credits` table with worked date, credit date, and expiry | Done |
| Backend Development | Build comp-off credit request and approval APIs | Done |
| Backend Development | Implement auto-detection of comp-off eligibility from attendance data | Done |
| Backend Development | Build comp-off balance and expiry tracking API | Done |
| Frontend Development | Build comp-off request form linked to attendance records | Done |
| Frontend Development | Build comp-off balance view with expiry indicators | Done |
| Integration Testing | Validate comp-off credit, usage, and expiry workflows | Done |
| UAT & QA | Conduct user acceptance testing for comp-off management | Not Started |
| Deployment | Deploy comp-off module to staging and production | Not Started |

### 3.6 Leave Planner & Calendar

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define calendar views (personal, team, department, organization) | Done |
| Requirements | Define holiday calendar setup and regional holiday support | Done |
| Database Design | Design `holidays` table with date, name, type, and applicable regions | Done |
| Database Design | Design `leave_plans` table for tentative future leave entries | Done |
| Backend Development | Build holiday calendar CRUD APIs with region support | Done |
| Backend Development | Build team leave calendar API aggregating approved and planned leaves | Done |
| Backend Development | Build leave planning API for tentative bookings | Done |
| Frontend Development | Build interactive calendar view with color-coded leave types | Done |
| Frontend Development | Build holiday calendar setup interface for HR | Done |
| Frontend Development | Build team availability view showing coverage gaps | Done |
| Integration Testing | Validate calendar data aggregation and coverage calculations | Done |
| UAT & QA | Conduct user acceptance testing for leave planner and calendar | Not Started |
| Deployment | Deploy leave planner module to staging and production | Not Started |

### 3.7 Leave Analytics Reports

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define analytics metrics (absenteeism rate, leave trends, utilization rate) | Done |
| Requirements | Define report dimensions (department, designation, gender, age group) | Done |
| Database Design | Design materialized views or summary tables for analytics performance | Done |
| Backend Development | Build leave trend analysis API (monthly/quarterly comparisons) | Done |
| Backend Development | Build absenteeism rate computation API by department and role | Done |
| Backend Development | Build leave utilization report API (consumed vs. entitled) | Done |
| Backend Development | Implement report export service (PDF, Excel) | Done |
| Frontend Development | Build analytics dashboard with charts (bar, line, pie) and KPI cards | Done |
| Frontend Development | Build drill-down views from summary to individual employee level | Done |
| Frontend Development | Add date range selector and department filter controls | Done |
| Integration Testing | Validate analytics accuracy against transactional leave data | Done |
| UAT & QA | Conduct user acceptance testing for leave analytics reports | Not Started |
| Deployment | Deploy leave analytics module to staging and production | Not Started |

---

## Milestone 4: Payroll Processing

### 4.1 Salary Structure Configuration

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define salary components (basic, HRA, DA, conveyance, special allowances) | Not Started |
| Requirements | Define computation rules (fixed, percentage of basic, slab-based) | Not Started |
| Requirements | Define salary structure templates by grade and designation | Not Started |
| Database Design | Design `salary_components` table with type (earning/deduction), taxability flag | Done |
| Database Design | Design `salary_structures` table linking components with computation formulas | Done |
| Database Design | Design `employee_salary_details` table for individual salary assignments | Done |
| Backend Development | Build CRUD APIs for salary component master | Not Started |
| Backend Development | Build salary structure template creation and cloning APIs | Not Started |
| Backend Development | Build employee salary assignment API with effective date support | Not Started |
| Backend Development | Implement salary revision API with history tracking | Not Started |
| Frontend Development | Build salary component master list and form | Not Started |
| Frontend Development | Build salary structure template designer with formula builder | Not Started |
| Frontend Development | Build employee salary assignment form with CTC breakdown preview | Not Started |
| Integration Testing | Validate salary computation across different structure configurations | Not Started |
| UAT & QA | Conduct user acceptance testing for salary structure setup | Not Started |
| Deployment | Deploy salary structure module to staging and production | Not Started |

### 4.2 Monthly Payroll Computation

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define payroll cycle (cut-off dates, processing schedule) | Not Started |
| Requirements | Define inputs to payroll (attendance, leaves, overtime, deductions, arrears) | Not Started |
| Requirements | Define payroll locks, verification, and finalization workflow | Not Started |
| Database Design | Design `payroll_runs` table with period, status, and processing metadata | Not Started |
| Database Design | Design `payroll_details` table with component-wise breakup per employee | Not Started |
| Database Design | Design `payroll_adjustments` table for ad-hoc additions/deductions | Not Started |
| Backend Development | Build payroll initialization API (pull attendance, leave, overtime data) | Not Started |
| Backend Development | Build gross salary computation engine (apply structure to each employee) | Not Started |
| Backend Development | Build deduction computation engine (PF, ESI, TDS, loan EMIs, advances) | Not Started |
| Backend Development | Build net salary computation and rounding logic | Not Started |
| Backend Development | Implement payroll verification and finalization workflow APIs | Not Started |
| Backend Development | Build payroll reversal API for error correction before finalization | Not Started |
| Frontend Development | Build payroll processing dashboard with step-by-step wizard | Not Started |
| Frontend Development | Build payroll review grid with component-wise columns and variance flags | Not Started |
| Frontend Development | Build adjustment entry interface for ad-hoc items | Not Started |
| Frontend Development | Build payroll comparison view (current vs. previous month) | Not Started |
| Integration Testing | Validate payroll computation accuracy with sample datasets | Not Started |
| Integration Testing | Verify attendance and leave data integration into payroll | Not Started |
| UAT & QA | Conduct parallel payroll run and reconcile with existing system | Not Started |
| Deployment | Deploy payroll computation module to staging and production | Not Started |

### 4.3 TDS & Statutory Deductions

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define income tax slab configurations (old and new regime) | Not Started |
| Requirements | Define investment declaration and proof submission workflows | Not Started |
| Requirements | Define professional tax slab configurations by state | Not Started |
| Database Design | Design `tax_slabs` table with regime, slab ranges, and rates | Not Started |
| Database Design | Design `investment_declarations` table with section-wise declared amounts | Not Started |
| Database Design | Design `tax_computations` table with projected and actual TDS per month | Not Started |
| Backend Development | Build tax regime selection and investment declaration APIs | Not Started |
| Backend Development | Build projected annual income computation engine | Not Started |
| Backend Development | Build monthly TDS computation engine (spread across remaining months) | Not Started |
| Backend Development | Build proof submission and verification APIs | Not Started |
| Backend Development | Implement professional tax computation by employee state | Not Started |
| Backend Development | Build Form 16 / Form 12BB data generation APIs | Not Started |
| Frontend Development | Build investment declaration form with section-wise inputs | Not Started |
| Frontend Development | Build tax computation summary view with month-wise TDS breakup | Not Started |
| Frontend Development | Build proof submission upload interface | Not Started |
| Integration Testing | Validate TDS computation against multiple income and investment scenarios | Not Started |
| UAT & QA | Conduct user acceptance testing with real salary and investment data | Not Started |
| Deployment | Deploy TDS and statutory deduction module to staging and production | Not Started |

### 4.4 PF / ESI Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define PF computation rules (employee share, employer share, admin charges) | Not Started |
| Requirements | Define ESI eligibility thresholds and contribution rates | Not Started |
| Requirements | Define PF/ESI return filing and challan generation requirements | Not Started |
| Database Design | Design `pf_contributions` table with employee and employer shares | Not Started |
| Database Design | Design `esi_contributions` table with employee and employer shares | Not Started |
| Database Design | Design `pf_esi_challans` table for payment tracking | Not Started |
| Backend Development | Build PF computation engine (basic + DA based calculation) | Not Started |
| Backend Development | Build ESI computation engine with eligibility checks | Not Started |
| Backend Development | Build PF/ESI return file generation (ECR format for PF, ESIC format) | Not Started |
| Backend Development | Build challan generation and payment tracking APIs | Not Started |
| Frontend Development | Build PF/ESI configuration panel with rate and threshold settings | Not Started |
| Frontend Development | Build monthly PF/ESI summary report view | Not Started |
| Frontend Development | Build return file generation and download interface | Not Started |
| Integration Testing | Validate PF/ESI calculations against statutory rules | Not Started |
| UAT & QA | Conduct user acceptance testing and reconciliation with government portals | Not Started |
| Deployment | Deploy PF/ESI module to staging and production | Not Started |

### 4.5 Pay Slip Generation

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define pay slip template layout and branding requirements | Not Started |
| Requirements | Define pay slip distribution methods (email, portal, print) | Not Started |
| Database Design | Design `payslip_templates` table with format and layout configuration | Not Started |
| Database Design | Design `payslip_distribution_logs` table for tracking delivery status | Not Started |
| Backend Development | Build pay slip PDF generation engine with template support | Not Started |
| Backend Development | Build bulk pay slip generation API (entire payroll run) | Not Started |
| Backend Development | Build pay slip email distribution service | Not Started |
| Backend Development | Build pay slip download API for employee self-service | Not Started |
| Frontend Development | Build pay slip template designer with drag-and-drop fields | Not Started |
| Frontend Development | Build employee pay slip viewer with month/year selector | Not Started |
| Frontend Development | Build bulk distribution trigger and status tracking for HR | Not Started |
| Integration Testing | Validate pay slip content against payroll computation data | Not Started |
| UAT & QA | Conduct user acceptance testing for pay slip generation and distribution | Not Started |
| Deployment | Deploy pay slip module to staging and production | Not Started |

### 4.6 Bank Transfer Integration

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define supported bank formats (NEFT, RTGS, IMPS batch file formats) | Not Started |
| Requirements | Define bank account validation and IFSC code verification requirements | Not Started |
| Database Design | Design `bank_accounts` table with account number, IFSC, and bank name | Not Started |
| Database Design | Design `salary_disbursements` table with batch ID, UTR, and status | Not Started |
| Backend Development | Build bank file generation API (format-specific: HDFC, ICICI, SBI, etc.) | Not Started |
| Backend Development | Build salary disbursement batch creation and tracking APIs | Not Started |
| Backend Development | Implement bank account validation service (IFSC lookup, penny testing) | Not Started |
| Backend Development | Build disbursement reconciliation API (match UTR with bank statements) | Not Started |
| Frontend Development | Build bank file generation and download interface | Not Started |
| Frontend Development | Build disbursement tracking dashboard with batch-wise status | Not Started |
| Frontend Development | Build reconciliation interface for matching payments | Not Started |
| Integration Testing | Validate bank file format compliance with multiple banks | Not Started |
| UAT & QA | Conduct end-to-end testing with bank sandbox environments | Not Started |
| Deployment | Deploy bank transfer module to staging and production | Not Started |

### 4.7 Payroll Audit Reports

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define audit report types (variance, statutory, reconciliation, cost-to-company) | Not Started |
| Requirements | Define report access controls and data sensitivity levels | Not Started |
| Database Design | Design `payroll_audit_logs` table for tracking all payroll data changes | Not Started |
| Backend Development | Build payroll variance report API (month-on-month, employee-wise) | Not Started |
| Backend Development | Build statutory compliance report API (PF, ESI, PT, TDS summaries) | Not Started |
| Backend Development | Build cost-to-company report API by department, project, and cost center | Not Started |
| Backend Development | Build payroll reconciliation report API (computed vs. disbursed) | Not Started |
| Backend Development | Implement report export service (PDF, Excel) with access controls | Not Started |
| Frontend Development | Build payroll reports menu with category-wise report listing | Not Started |
| Frontend Development | Build interactive report viewers with filter and drill-down capabilities | Not Started |
| Frontend Development | Build scheduled report configuration interface | Not Started |
| Integration Testing | Validate report data accuracy against payroll and statutory records | Not Started |
| UAT & QA | Conduct user acceptance testing for all payroll audit reports | Not Started |
| Deployment | Deploy payroll audit reports module to staging and production | Not Started |

---

## Milestone 5: Recruitment & Onboarding

### 5.1 Job Posting & Applicant Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define job posting fields (title, description, qualifications, experience, etc.) | Not Started |
| Requirements | Define applicant pipeline stages (applied, screened, shortlisted, rejected) | Not Started |
| Requirements | Define job board and career page integration requirements | Not Started |
| Database Design | Design `job_postings` table with position, department, and requirements | Not Started |
| Database Design | Design `applicants` table with personal details and resume attachment | Not Started |
| Database Design | Design `applicant_stages` table for tracking pipeline progression | Not Started |
| Backend Development | Build CRUD APIs for job posting management | Not Started |
| Backend Development | Build applicant submission API (internal portal and external career page) | Not Started |
| Backend Development | Build applicant pipeline management API (stage transitions) | Not Started |
| Backend Development | Implement resume parsing and keyword matching service | Not Started |
| Backend Development | Build applicant search and filter API (skills, experience, location) | Not Started |
| Frontend Development | Build job posting creation form with rich text editor for description | Not Started |
| Frontend Development | Build applicant pipeline board (Kanban-style drag-and-drop) | Not Started |
| Frontend Development | Build applicant detail view with resume viewer and notes | Not Started |
| Frontend Development | Build career page widget for external job applications | Not Started |
| Integration Testing | Validate end-to-end posting-to-application-to-pipeline workflow | Not Started |
| UAT & QA | Conduct user acceptance testing for job posting and applicant tracking | Not Started |
| Deployment | Deploy recruitment module to staging and production | Not Started |

### 5.2 Interview Scheduling

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define interview types (phone screen, technical, HR, panel, case study) | Not Started |
| Requirements | Define interviewer availability and calendar integration requirements | Not Started |
| Database Design | Design `interviews` table with type, date/time, interviewer, and location/link | Not Started |
| Database Design | Design `interview_feedback` table with rating scales and comments | Not Started |
| Backend Development | Build interview scheduling API with conflict detection | Not Started |
| Backend Development | Build calendar integration service (Google Calendar, Outlook) | Not Started |
| Backend Development | Build automated email/SMS notifications to candidate and interviewers | Not Started |
| Backend Development | Build interview feedback submission and aggregation APIs | Not Started |
| Frontend Development | Build interview scheduling form with time slot picker and interviewer selector | Not Started |
| Frontend Development | Build interview calendar view with availability overlay | Not Started |
| Frontend Development | Build feedback form with structured rating criteria | Not Started |
| Integration Testing | Validate scheduling, notification, and feedback collection workflows | Not Started |
| UAT & QA | Conduct user acceptance testing for interview scheduling | Not Started |
| Deployment | Deploy interview scheduling module to staging and production | Not Started |

### 5.3 Offer Letter Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define offer letter template fields and approval workflow | Not Started |
| Requirements | Define compensation negotiation tracking requirements | Not Started |
| Database Design | Design `offer_letters` table with candidate, position, CTC, and status | Not Started |
| Database Design | Design `offer_templates` table with configurable clause blocks | Not Started |
| Backend Development | Build offer letter generation API with template merge | Not Started |
| Backend Development | Build offer approval workflow API (multi-level) | Not Started |
| Backend Development | Build offer dispatch API (email with PDF attachment) | Not Started |
| Backend Development | Build offer acceptance/decline tracking API | Not Started |
| Frontend Development | Build offer letter template editor with variable placeholders | Not Started |
| Frontend Development | Build offer creation form with CTC breakup calculator | Not Started |
| Frontend Development | Build offer tracking dashboard (sent, accepted, declined, expired) | Not Started |
| Integration Testing | Validate offer generation, dispatch, and acceptance tracking | Not Started |
| UAT & QA | Conduct user acceptance testing for offer letter management | Not Started |
| Deployment | Deploy offer letter module to staging and production | Not Started |

### 5.4 Onboarding Checklist

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define onboarding checklist items by role and department | Not Started |
| Requirements | Define checklist ownership (HR, IT, admin, reporting manager) | Not Started |
| Database Design | Design `onboarding_templates` table with role-specific checklist items | Not Started |
| Database Design | Design `onboarding_tasks` table with assignee, due date, and completion status | Not Started |
| Backend Development | Build onboarding initiation API (triggered on offer acceptance) | Not Started |
| Backend Development | Build checklist task assignment and tracking APIs | Not Started |
| Backend Development | Build task completion and sign-off APIs | Not Started |
| Backend Development | Implement automated reminders for pending onboarding tasks | Not Started |
| Frontend Development | Build onboarding checklist template configuration for HR | Not Started |
| Frontend Development | Build new joiner onboarding dashboard with task progress | Not Started |
| Frontend Development | Build task completion interface for each stakeholder | Not Started |
| Integration Testing | Validate onboarding workflow from offer acceptance to completion | Not Started |
| UAT & QA | Conduct user acceptance testing for onboarding checklist | Not Started |
| Deployment | Deploy onboarding module to staging and production | Not Started |

### 5.5 Background Verification

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define verification types (education, employment, criminal, address, reference) | Not Started |
| Requirements | Define vendor integration requirements for background check agencies | Not Started |
| Database Design | Design `background_checks` table with type, vendor, status, and findings | Not Started |
| Database Design | Design `verification_documents` table for uploaded proof documents | Not Started |
| Backend Development | Build background check initiation and tracking APIs | Not Started |
| Backend Development | Build vendor integration API for automated check submission | Not Started |
| Backend Development | Build verification result recording and escalation APIs | Not Started |
| Backend Development | Implement status notification triggers (cleared, flagged, pending) | Not Started |
| Frontend Development | Build background check initiation form with type selection | Not Started |
| Frontend Development | Build verification tracking dashboard with status indicators | Not Started |
| Frontend Development | Build verification result review and approval interface | Not Started |
| Integration Testing | Validate end-to-end verification workflow with mock vendor responses | Not Started |
| UAT & QA | Conduct user acceptance testing for background verification | Not Started |
| Deployment | Deploy background verification module to staging and production | Not Started |

### 5.6 Induction Program Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define induction program structure (sessions, presenters, materials) | Not Started |
| Requirements | Define attendance and completion tracking requirements | Not Started |
| Database Design | Design `induction_programs` table with schedule, sessions, and capacity | Not Started |
| Database Design | Design `induction_attendees` table with attendance and feedback | Not Started |
| Backend Development | Build induction program creation and scheduling APIs | Not Started |
| Backend Development | Build attendee registration and attendance marking APIs | Not Started |
| Backend Development | Build induction material distribution API (documents, presentations) | Not Started |
| Backend Development | Build induction completion certificate generation API | Not Started |
| Frontend Development | Build induction program calendar and session detail view | Not Started |
| Frontend Development | Build attendee registration and attendance interface | Not Started |
| Frontend Development | Build induction feedback form | Not Started |
| Integration Testing | Validate induction scheduling, attendance, and completion workflows | Not Started |
| UAT & QA | Conduct user acceptance testing for induction program management | Not Started |
| Deployment | Deploy induction module to staging and production | Not Started |

### 5.7 Recruitment Analytics

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define recruitment KPIs (time-to-hire, cost-per-hire, source effectiveness) | Not Started |
| Requirements | Define analytics dimensions (department, position, source, recruiter) | Not Started |
| Database Design | Design summary tables or views for recruitment metrics aggregation | Not Started |
| Backend Development | Build time-to-hire analysis API (stage-wise duration breakup) | Not Started |
| Backend Development | Build source effectiveness report API (applications and hires by source) | Not Started |
| Backend Development | Build recruiter performance report API | Not Started |
| Backend Development | Build pipeline funnel analysis API (drop-off at each stage) | Not Started |
| Backend Development | Implement report export service (PDF, Excel) | Not Started |
| Frontend Development | Build recruitment analytics dashboard with KPI cards and trend charts | Not Started |
| Frontend Development | Build funnel visualization for pipeline analysis | Not Started |
| Frontend Development | Build drill-down views from summary to individual requisition level | Not Started |
| Integration Testing | Validate analytics accuracy against transactional recruitment data | Not Started |
| UAT & QA | Conduct user acceptance testing for recruitment analytics | Not Started |
| Deployment | Deploy recruitment analytics module to staging and production | Not Started |

---

## Milestone 6: Training & Development

### 6.1 Training Calendar Management

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define training categories (technical, soft skills, compliance, leadership) | Not Started |
| Requirements | Define calendar management rules (scheduling, capacity, conflicts) | Not Started |
| Database Design | Design `training_programs` table with title, category, trainer, and schedule | Not Started |
| Database Design | Design `training_sessions` table with date, time, venue, and capacity | Not Started |
| Database Design | Design `training_enrollments` table with employee, session, and status | Not Started |
| Backend Development | Build CRUD APIs for training program and session management | Not Started |
| Backend Development | Build enrollment API with capacity validation and waitlist support | Not Started |
| Backend Development | Build calendar aggregation API with filters (category, department, trainer) | Not Started |
| Backend Development | Implement automated notifications for upcoming sessions | Not Started |
| Frontend Development | Build training calendar view (monthly, weekly) with session details | Not Started |
| Frontend Development | Build training program creation wizard with session scheduler | Not Started |
| Frontend Development | Build enrollment interface with available seats indicator | Not Started |
| Integration Testing | Validate session scheduling, enrollment, and notification workflows | Not Started |
| UAT & QA | Conduct user acceptance testing for training calendar management | Not Started |
| Deployment | Deploy training calendar module to staging and production | Not Started |

### 6.2 CME / Skill-Based Training

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define CME (Continuing Medical Education) credit requirements by role | Not Started |
| Requirements | Define skill matrix and competency framework for skill-based training | Not Started |
| Database Design | Design `cme_credits` table with employee, program, credits earned, and period | Not Started |
| Database Design | Design `skill_matrix` table with skill, proficiency level, and target | Not Started |
| Database Design | Design `training_skill_mapping` table linking programs to skills | Not Started |
| Backend Development | Build CME credit tracking and compliance reporting APIs | Not Started |
| Backend Development | Build skill gap analysis API (current vs. required proficiency) | Not Started |
| Backend Development | Build training recommendation engine based on skill gaps | Not Started |
| Backend Development | Build CME compliance alert service for approaching deadlines | Not Started |
| Frontend Development | Build CME credit dashboard with compliance status indicators | Not Started |
| Frontend Development | Build skill matrix view with proficiency level visualization | Not Started |
| Frontend Development | Build training recommendation list based on skill gap analysis | Not Started |
| Integration Testing | Validate CME credit computation and skill gap analysis accuracy | Not Started |
| UAT & QA | Conduct user acceptance testing for CME and skill-based training | Not Started |
| Deployment | Deploy CME and skill training module to staging and production | Not Started |

### 6.3 Training Attendance Tracking

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define attendance marking methods (manual, QR code, biometric) | Not Started |
| Requirements | Define minimum attendance requirements for completion certification | Not Started |
| Database Design | Design `training_attendance` table with session, employee, and timestamp | Not Started |
| Backend Development | Build attendance marking API (manual entry by trainer) | Not Started |
| Backend Development | Build QR code generation and scan-based attendance API | Not Started |
| Backend Development | Build attendance summary API (session-wise and employee-wise) | Not Started |
| Backend Development | Implement completion status computation based on attendance threshold | Not Started |
| Frontend Development | Build trainer attendance marking interface with employee roster | Not Started |
| Frontend Development | Build QR code display and scanner interface for self-check-in | Not Started |
| Frontend Development | Build attendance summary report view | Not Started |
| Integration Testing | Validate attendance marking, summary computation, and completion logic | Not Started |
| UAT & QA | Conduct user acceptance testing for training attendance tracking | Not Started |
| Deployment | Deploy training attendance module to staging and production | Not Started |

### 6.4 Certification & Renewal Alerts

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define certification types and renewal period rules | Not Started |
| Requirements | Define alert schedule (90 days, 60 days, 30 days, 7 days before expiry) | Not Started |
| Database Design | Design `certifications` table with name, issuer, issue date, and expiry date | Not Started |
| Database Design | Design `certification_alerts` table with alert date, type, and delivery status | Not Started |
| Backend Development | Build certification record CRUD APIs | Not Started |
| Backend Development | Build scheduled alert generation job (check expiry dates daily) | Not Started |
| Backend Development | Build alert delivery service (email, SMS, in-app notification) | Not Started |
| Backend Development | Build certification renewal tracking API (renewed, pending, expired) | Not Started |
| Frontend Development | Build certification list view with expiry status color coding | Not Started |
| Frontend Development | Build certification upload form with expiry date and reminder settings | Not Started |
| Frontend Development | Build alert management dashboard for HR (upcoming expiries by department) | Not Started |
| Integration Testing | Validate alert generation timing and delivery across channels | Not Started |
| UAT & QA | Conduct user acceptance testing for certification and renewal alerts | Not Started |
| Deployment | Deploy certification alerts module to staging and production | Not Started |

### 6.5 E-Learning Integration

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define LMS (Learning Management System) integration requirements | Not Started |
| Requirements | Define SCORM/xAPI content compatibility and tracking standards | Not Started |
| Database Design | Design `elearning_courses` table with title, provider, URL, and duration | Not Started |
| Database Design | Design `elearning_progress` table with employee, course, progress %, and score | Not Started |
| Backend Development | Build LMS integration adapter (API connector for popular LMS platforms) | Not Started |
| Backend Development | Build course catalog sync API (pull courses from external LMS) | Not Started |
| Backend Development | Build progress tracking API (receive completion events from LMS) | Not Started |
| Backend Development | Build course assignment API (mandatory and elective courses) | Not Started |
| Frontend Development | Build e-learning course catalog with search and category filters | Not Started |
| Frontend Development | Build course launcher with embedded player or external LMS redirect | Not Started |
| Frontend Development | Build learning progress dashboard with completion status and scores | Not Started |
| Integration Testing | Validate LMS integration with at least two major LMS platforms | Not Started |
| UAT & QA | Conduct user acceptance testing for e-learning integration | Not Started |
| Deployment | Deploy e-learning module to staging and production | Not Started |

### 6.6 Training Feedback & Evaluation

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define feedback form structure (rating scales, open-ended questions) | Not Started |
| Requirements | Define evaluation methods (pre-test, post-test, Kirkpatrick model levels) | Not Started |
| Database Design | Design `feedback_forms` table with configurable question templates | Not Started |
| Database Design | Design `feedback_responses` table with ratings and comments | Not Started |
| Database Design | Design `training_evaluations` table with pre/post test scores | Not Started |
| Backend Development | Build feedback form builder API with question types (rating, text, MCQ) | Not Started |
| Backend Development | Build feedback submission and aggregation APIs | Not Started |
| Backend Development | Build pre/post training assessment APIs | Not Started |
| Backend Development | Build training effectiveness analysis API (score improvements, satisfaction) | Not Started |
| Frontend Development | Build feedback form builder interface for HR/trainers | Not Started |
| Frontend Development | Build feedback submission form for participants | Not Started |
| Frontend Development | Build training effectiveness dashboard with comparative charts | Not Started |
| Integration Testing | Validate feedback collection, aggregation, and effectiveness analysis | Not Started |
| UAT & QA | Conduct user acceptance testing for training feedback and evaluation | Not Started |
| Deployment | Deploy feedback and evaluation module to staging and production | Not Started |

### 6.7 Training Cost Reports

| Phase | Task | Status |
|-------|------|--------|
| Requirements | Define cost categories (trainer fee, venue, materials, travel, opportunity cost) | Not Started |
| Requirements | Define budget allocation and tracking requirements | Not Started |
| Database Design | Design `training_budgets` table with department, fiscal year, and allocated amount | Not Started |
| Database Design | Design `training_costs` table with program, category, and actual amount | Not Started |
| Backend Development | Build training budget allocation and tracking APIs | Not Started |
| Backend Development | Build cost recording API per training program and category | Not Started |
| Backend Development | Build cost analysis report API (budget vs. actual, cost per employee) | Not Started |
| Backend Development | Build ROI computation API (training cost vs. performance improvement) | Not Started |
| Backend Development | Implement report export service (PDF, Excel) | Not Started |
| Frontend Development | Build budget allocation form with department and fiscal year selectors | Not Started |
| Frontend Development | Build cost entry form with category breakdown | Not Started |
| Frontend Development | Build training cost analytics dashboard with budget vs. actual charts | Not Started |
| Frontend Development | Build ROI analysis view with trend visualization | Not Started |
| Integration Testing | Validate cost tracking accuracy and budget vs. actual computations | Not Started |
| UAT & QA | Conduct user acceptance testing for training cost reports | Not Started |
| Deployment | Deploy training cost reports module to staging and production | Not Started |
