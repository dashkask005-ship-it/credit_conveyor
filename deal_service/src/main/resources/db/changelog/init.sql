CREATE TABLE IF NOT EXISTS clients (
                                       id BIGSERIAL PRIMARY KEY,
                                       first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    birth_date DATE,
    work_experience INTEGER,
    monthly_income DECIMAL(19,2),
    employment_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS applications (
                                            id BIGSERIAL PRIMARY KEY,
                                            client_id BIGINT NOT NULL,
                                            amount DECIMAL(19,2),
    term INTEGER,
    purpose VARCHAR(100),
    status VARCHAR(20),
    rate DECIMAL(5,2),
    monthly_payment DECIMAL(19,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_client FOREIGN KEY (client_id) REFERENCES clients(id)
    );

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

CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_applications_client_id ON applications(client_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);