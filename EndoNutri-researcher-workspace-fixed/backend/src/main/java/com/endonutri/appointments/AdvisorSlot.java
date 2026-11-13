package com.endonutri.appointments;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="advisor_slots")
public class AdvisorSlot {
 @Id private UUID id;
 @Column(name="advisor_id",nullable=false) private UUID advisorId;
 @Column(name="starts_at",nullable=false) private Instant startsAt;
 @Column(name="ends_at",nullable=false) private Instant endsAt;
 @Column(nullable=false,length=16) private String status="OPEN";
 protected AdvisorSlot(){}
 public AdvisorSlot(UUID advisorId,Instant startsAt,Instant endsAt){this.id=UUID.randomUUID();this.advisorId=advisorId;this.startsAt=startsAt;this.endsAt=endsAt;}
 public UUID getId(){return id;} public UUID getAdvisorId(){return advisorId;}
 public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;}
 public String getStatus(){return status;} public void setStatus(String s){status=s;}
}
