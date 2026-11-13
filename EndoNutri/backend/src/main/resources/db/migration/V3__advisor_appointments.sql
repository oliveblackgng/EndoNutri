CREATE TABLE advisor_slots (
 id UUID PRIMARY KEY,
 advisor_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
 starts_at TIMESTAMPTZ NOT NULL,
 ends_at TIMESTAMPTZ NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','HELD','BOOKED')),
 CHECK (ends_at > starts_at)
);
CREATE INDEX idx_advisor_slots_open_time ON advisor_slots(status, starts_at);
CREATE TABLE appointment_requests (
 id UUID PRIMARY KEY,
 slot_id UUID NOT NULL REFERENCES advisor_slots(id) ON DELETE CASCADE,
 patient_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
 status VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACCEPTED','DECLINED')),
 patient_message VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_appointment_patient ON appointment_requests(patient_id, created_at DESC);
