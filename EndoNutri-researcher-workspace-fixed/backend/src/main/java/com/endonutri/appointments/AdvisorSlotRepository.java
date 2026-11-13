package com.endonutri.appointments;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.Instant;
import java.util.UUID;
public interface AdvisorSlotRepository extends JpaRepository<AdvisorSlot,UUID> {
 List<AdvisorSlot> findByAdvisorIdOrderByStartsAtAsc(UUID advisorId);
 List<AdvisorSlot> findByStatusAndStartsAtAfterOrderByStartsAtAsc(String status,Instant after);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select s from AdvisorSlot s where s.id=:id")
 Optional<AdvisorSlot> lockById(@Param("id") UUID id);
}
