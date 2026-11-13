package com.endonutri.patient;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
@Entity @Table(name="symptom_records")
public class SymptomRecord {
 @Id private UUID id;
 @Column(name="patient_id",nullable=false) private UUID patientId;
 @Column(nullable=false) private String symptom;
 @Column(nullable=false) private int severity;
 @Column(name="recorded_on",nullable=false) private LocalDate recordedOn;
 private String notes;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 protected SymptomRecord(){}
 public SymptomRecord(UUID patientId,String symptom,int severity,LocalDate date,String notes){this.id=UUID.randomUUID();this.patientId=patientId;this.symptom=symptom;this.severity=severity;this.recordedOn=date;this.notes=notes;this.createdAt=Instant.now();}
 public UUID getId(){return id;} public UUID getPatientId(){return patientId;} public String getSymptom(){return symptom;} public int getSeverity(){return severity;} public LocalDate getRecordedOn(){return recordedOn;} public String getNotes(){return notes;}
}
