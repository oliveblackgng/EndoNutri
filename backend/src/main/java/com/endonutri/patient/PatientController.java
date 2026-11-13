package com.endonutri.patient;

import com.endonutri.auth.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    private final SymptomRepository symptoms;
    private final PatientProfileRepository profiles;

    public PatientController(SymptomRepository symptoms, PatientProfileRepository profiles) {
        this.symptoms = symptoms;
        this.profiles = profiles;
    }

    public record SymptomInput(@NotBlank @Size(max=80) String symptom,
        @Min(0) @Max(10) int severity, @NotNull LocalDate recordedOn,
        @Size(max=1000) String notes) {}
    public record ConsentInput(@NotNull Boolean consent, @NotBlank String consentVersion) {}

    private UUID patientId(Authentication auth) {
        return ((AppUser) auth.getPrincipal()).getId();
    }

    @GetMapping("/symptoms")
    public List<SymptomRecord> list(Authentication auth) {
        return symptoms.findByPatientIdOrderByRecordedOnDesc(patientId(auth));
    }

    @PostMapping("/symptoms")
    public ResponseEntity<SymptomRecord> create(@Valid @RequestBody SymptomInput input, Authentication auth) {
        SymptomRecord saved = symptoms.save(new SymptomRecord(patientId(auth), input.symptom(),
            input.severity(), input.recordedOn(), input.notes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/research-consent")
    public ResponseEntity<?> consent(@Valid @RequestBody ConsentInput input, Authentication auth) {
        PatientProfile profile = profiles.findById(patientId(auth)).orElseGet(() -> new PatientProfile(patientId(auth)));
        profile.setResearchConsent(input.consent());
        profile.setConsentedAt(input.consent() ? Instant.now() : null);
        profile.setConsentVersion(input.consentVersion());
        profiles.save(profile);
        return ResponseEntity.ok(Map.of("researchConsent", profile.isResearchConsent(),
            "consentVersion", profile.getConsentVersion()));
    }

    @GetMapping("/research-consent")
    public ResponseEntity<?> getConsent(Authentication auth) {
        PatientProfile p = profiles.findById(patientId(auth)).orElse(null);
        return ResponseEntity.ok(Map.of("researchConsent", p != null && p.isResearchConsent(),
            "consentVersion", p == null || p.getConsentVersion() == null ? "" : p.getConsentVersion()));
    }

    @DeleteMapping("/symptoms/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id, Authentication auth) {
        SymptomRecord record = symptoms.findById(id).orElse(null);
        if (record == null) return ResponseEntity.notFound().build();
        if (!record.getPatientId().equals(patientId(auth))) return ResponseEntity.status(403).build();
        symptoms.delete(record);
        return ResponseEntity.noContent().build();
    }
}
