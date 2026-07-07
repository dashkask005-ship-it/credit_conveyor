CREATE TABLE IF NOT EXISTS offers (
                                      id BIGSERIAL PRIMARY KEY,
                                      application_id BIGINT,
                                      amount DECIMAL(19,2),
    rate DECIMAL(5,2),
    monthly_payment DECIMAL(19,2),
    term INTEGER,
    is_insurance_enabled BOOLEAN,
    is_salary_client BOOLEAN,
    description VARCHAR(255),
    priority INTEGER,
    CONSTRAINT fk_offer_application FOREIGN KEY (application_id) REFERENCES applications(id)
    );

CREATE INDEX IF NOT EXISTS idx_offers_application_id ON offers(application_id);