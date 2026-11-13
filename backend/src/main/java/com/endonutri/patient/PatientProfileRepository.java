package com.endonutri.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {}
