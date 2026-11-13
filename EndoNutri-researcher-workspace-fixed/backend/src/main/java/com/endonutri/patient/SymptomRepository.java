package com.endonutri.patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SymptomRepository extends JpaRepository<SymptomRecord,UUID>{
 List<SymptomRecord> findByPatientIdOrderByRecordedOnDesc(UUID patientId);
 long countDistinctPatientIdByPatientIdIn(Collection<UUID> ids);
}
