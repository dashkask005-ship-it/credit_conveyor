
CREATE TABLE IF NOT EXISTS credits (
                                       id BIGSERIAL PRIMARY KEY,
                                       application_id BIGINT,
                                       amount DECIMAL(19,2),
    rate DECIMAL(5,2),
    monthly_payment DECIMAL(19,2),
    term INTEGER,
    total_amount DECIMAL(19,2),
    status VARCHAR(20),
    issue_date DATE,
    CONSTRAINT fk_credit_application FOREIGN KEY (application_id) REFERENCES applications(id)
    );

CREATE INDEX IF NOT EXISTS idx_credits_application_id ON credits(application_id);