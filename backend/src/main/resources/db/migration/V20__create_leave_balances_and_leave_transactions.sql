-- Leave Balance Tracking: balances and transaction ledger

CREATE TABLE leave_balances (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT        NOT NULL,
    leave_type_id   BIGINT        NOT NULL,
    "year"          INT           NOT NULL,
    credited        DECIMAL(7, 2) NOT NULL DEFAULT 0,
    used            DECIMAL(7, 2) NOT NULL DEFAULT 0,
    pending         DECIMAL(7, 2) NOT NULL DEFAULT 0,
    available       DECIMAL(7, 2) NOT NULL DEFAULT 0,
    carry_forwarded DECIMAL(7, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_leave_balance_emp_type_year UNIQUE (employee_id, leave_type_id, "year"),
    CONSTRAINT fk_leave_balance_employee   FOREIGN KEY (employee_id)   REFERENCES employees(id),
    CONSTRAINT fk_leave_balance_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE INDEX idx_leave_balance_employee   ON leave_balances (employee_id);
CREATE INDEX idx_leave_balance_leave_type ON leave_balances (leave_type_id);
CREATE INDEX idx_leave_balance_year       ON leave_balances ("year");

CREATE TABLE leave_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id      BIGINT        NOT NULL,
    leave_type_id    BIGINT        NOT NULL,
    transaction_type VARCHAR(30)   NOT NULL,
    amount           DECIMAL(7, 2) NOT NULL,
    balance_after    DECIMAL(7, 2) NOT NULL,
    reference_type   VARCHAR(30),
    reference_id     BIGINT,
    description      VARCHAR(500),
    created_by       VARCHAR(100),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_leave_txn_employee   FOREIGN KEY (employee_id)   REFERENCES employees(id),
    CONSTRAINT fk_leave_txn_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
);

CREATE INDEX idx_leave_txn_employee   ON leave_transactions (employee_id);
CREATE INDEX idx_leave_txn_leave_type ON leave_transactions (leave_type_id);
CREATE INDEX idx_leave_txn_type       ON leave_transactions (transaction_type);
