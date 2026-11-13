package com.endonutri.appointments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest,UUID> {
 List<AppointmentRequest> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
 List<AppointmentRequest> findBySlotIdInOrderByCreatedAtDesc(Collection<UUID> slotIds);
}
