package com.endonutri.patient;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="patient_profiles")
public class PatientProfile {
    @Id @Column(name="user_id") private UUID userId;
    @Column(name="research_consent", nullable=false) private boolean researchConsent = false;
    @Column(name="consented_at") private Instant consentedAt;
    @Column(name="consent_version") private String consentVersion;
    protected PatientProfile() {}
    public PatientProfile(UUID userId) { this.userId = userId; }
    public UUID getUserId() { return userId; }
    public boolean isResearchConsent() { return researchConsent; }
    public void setResearchConsent(boolean value) { this.researchConsent = value; }
    public Instant getConsentedAt() { return consentedAt; }
    public void setConsentedAt(Instant value) { this.consentedAt = value; }
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String value) { this.consentVersion = value; }
}
