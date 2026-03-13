-- TDS & Statutory Deductions

CREATE TABLE tax_slabs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    regime VARCHAR(10) NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    slab_from DECIMAL(14,2) NOT NULL,
    slab_to DECIMAL(14,2),
    rate DECIMAL(5,2) NOT NULL,
    description VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tax_slab UNIQUE (regime, financial_year, slab_from)
);

CREATE TABLE investment_declarations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    regime VARCHAR(10) NOT NULL DEFAULT 'NEW',
    total_declared_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_verified_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    submitted_at TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_decl_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uq_inv_decl_employee_fy UNIQUE (employee_id, financial_year)
);

CREATE TABLE investment_declaration_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    declaration_id BIGINT NOT NULL,
    section VARCHAR(20) NOT NULL,
    description VARCHAR(200) NOT NULL,
    declared_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    verified_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    proof_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    proof_document_name VARCHAR(200),
    proof_remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_decl_item_decl FOREIGN KEY (declaration_id) REFERENCES investment_declarations(id)
);

CREATE TABLE tax_computations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    month INT NOT NULL CHECK (month BETWEEN 1 AND 12),
    gross_annual_income DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_exemptions DECIMAL(14,2) NOT NULL DEFAULT 0,
    taxable_income DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_annual_tax DECIMAL(14,2) NOT NULL DEFAULT 0,
    monthly_tds DECIMAL(14,2) NOT NULL DEFAULT 0,
    cess DECIMAL(14,2) NOT NULL DEFAULT 0,
    surcharge DECIMAL(14,2) NOT NULL DEFAULT 0,
    tds_deducted_till_date DECIMAL(14,2) NOT NULL DEFAULT 0,
    remaining_tds DECIMAL(14,2) NOT NULL DEFAULT 0,
    regime VARCHAR(10) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tax_comp_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uq_tax_comp_employee_fy_month UNIQUE (employee_id, financial_year, month)
);

CREATE TABLE professional_tax_slabs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state VARCHAR(100) NOT NULL,
    slab_from DECIMAL(14,2) NOT NULL,
    slab_to DECIMAL(14,2),
    monthly_tax DECIMAL(10,2) NOT NULL,
    february_tax DECIMAL(10,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pt_slab_state UNIQUE (state, slab_from)
);

CREATE INDEX idx_inv_decl_employee ON investment_declarations(employee_id);
CREATE INDEX idx_inv_decl_items_decl ON investment_declaration_items(declaration_id);
CREATE INDEX idx_tax_comp_employee ON tax_computations(employee_id);
CREATE INDEX idx_tax_comp_fy ON tax_computations(financial_year);
CREATE INDEX idx_tax_slabs_regime_fy ON tax_slabs(regime, financial_year);
CREATE INDEX idx_pt_slabs_state ON professional_tax_slabs(state);
