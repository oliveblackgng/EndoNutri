ALTER TABLE patient_profiles
  ADD CONSTRAINT fk_patient_profile_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE;
CREATE INDEX idx_patient_profiles_research_consent ON patient_profiles(research_consent) WHERE research_consent = TRUE;
