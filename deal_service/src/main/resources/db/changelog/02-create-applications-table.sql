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

CREATE INDEX IF NOT EXISTS idx_applications_client_id ON applications(client_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);