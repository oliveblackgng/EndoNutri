CREATE TABLE app_users (
 id UUID PRIMARY KEY,
 full_name VARCHAR(140) NOT NULL,
 email VARCHAR(254) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 role VARCHAR(20) NOT NULL CHECK (role IN ('PATIENT','RESEARCHER','ADVISOR','ADMIN')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE patient_profiles (
 user_id UUID PRIMARY KEY REFERENCES app_users(id) ON DELETE CASCADE,
 age_range VARCHAR(30),
 research_consent BOOLEAN NOT NULL DEFAULT FALSE,
 consented_at TIMESTAMPTZ,
 consent_version VARCHAR(30)
);
CREATE TABLE symptom_records (
 id UUID PRIMARY KEY,
 patient_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
 symptom VARCHAR(80) NOT NULL,
 severity INTEGER NOT NULL CHECK (severity BETWEEN 0 AND 10),
 recorded_on DATE NOT NULL,
 notes VARCHAR(1000),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_symptom_patient_date ON symptom_records(patient_id, recorded_on DESC);
CREATE TABLE research_datasets (
 id UUID PRIMARY KEY,
 owner_id UUID NOT NULL REFERENCES app_users(id),
 original_filename VARCHAR(255) NOT NULL,
 stored_filename VARCHAR(255) NOT NULL,
 row_count INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE audit_events (
 id UUID PRIMARY KEY,
 actor_id UUID REFERENCES app_users(id),
 action VARCHAR(100) NOT NULL,
 resource_type VARCHAR(80) NOT NULL,
 resource_id VARCHAR(100),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
