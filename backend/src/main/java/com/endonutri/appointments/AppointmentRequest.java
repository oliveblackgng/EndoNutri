package com.endonutri.appointments;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="appointment_requests")
public class AppointmentRequest {
 @Id private UUID id;
 @Column(name="slot_id",nullable=false,unique=true) private UUID slotId;
 @Column(name="patient_id",nullable=false) private UUID patientId;
 @Column(nullable=false,length=16) private String status="PENDING";
 @Column(name="patient_message",length=500) private String patientMessage;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 protected AppointmentRequest(){}
 public AppointmentRequest(UUID slotId,UUID patientId,String message){this.id=UUID.randomUUID();this.slotId=slotId;this.patientId=patientId;this.patientMessage=message;this.createdAt=Instant.now();}
 public UUID getId(){return id;} public UUID getSlotId(){return slotId;} public UUID getPatientId(){return patientId;}
 public String getStatus(){return status;} public void setStatus(String s){status=s;}
 public String getPatientMessage(){return patientMessage;} public Instant getCreatedAt(){return createdAt;}
}
