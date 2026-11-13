package com.endonutri.appointments;

import com.endonutri.auth.AppUser;
import com.endonutri.auth.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/advisor")
public class AdvisorController {
 private final AdvisorSlotRepository slots; private final AppointmentRequestRepository requests; private final UserRepository users;
 public AdvisorController(AdvisorSlotRepository slots,AppointmentRequestRepository requests,UserRepository users){this.slots=slots;this.requests=requests;this.users=users;}
 private UUID id(Authentication a){return ((AppUser)a.getPrincipal()).getId();}
 public record SlotInput(@NotNull Instant startsAt,@NotNull Instant endsAt){}
 public record DecisionInput(@NotBlank String decision){}
 @GetMapping("/slots") public List<AdvisorSlot> mySlots(Authentication a){return slots.findByAdvisorIdOrderByStartsAtAsc(id(a));}
 @PostMapping("/slots") public ResponseEntity<?> createSlot(@Valid @RequestBody SlotInput in,Authentication a){
  if(!in.endsAt().isAfter(in.startsAt())||!in.startsAt().isAfter(Instant.now())) return ResponseEntity.badRequest().body(Map.of("error","Choose a future time range with an end after its start."));
  return ResponseEntity.status(201).body(slots.save(new AdvisorSlot(id(a),in.startsAt(),in.endsAt())));
 }
 @GetMapping("/requests") public List<Map<String,Object>> requests(Authentication a){
  List<AdvisorSlot> mine=slots.findByAdvisorIdOrderByStartsAtAsc(id(a));
  Map<UUID,AdvisorSlot> byId=new HashMap<>(); mine.forEach(s->byId.put(s.getId(),s));
  List<AppointmentRequest> all=requests.findBySlotIdInOrderByCreatedAtDesc(byId.keySet());
  List<Map<String,Object>> out=new ArrayList<>();
  for(AppointmentRequest r:all){AppUser patient=users.findById(r.getPatientId()).orElse(null);AdvisorSlot slot=byId.get(r.getSlotId());
   out.add(Map.of("id",r.getId(),"status",r.getStatus(),"patientName",patient==null?"Patient":patient.getFullName(),"patientMessage",Objects.toString(r.getPatientMessage(),""),"startsAt",slot.getStartsAt(),"endsAt",slot.getEndsAt()));}
  return out;
 }
 @PostMapping("/requests/{requestId}/decision") @Transactional
 public ResponseEntity<?> decide(@PathVariable UUID requestId,@Valid @RequestBody DecisionInput in,Authentication a){
  AppointmentRequest r=requests.findById(requestId).orElse(null); if(r==null)return ResponseEntity.notFound().build();
  AdvisorSlot slot=slots.lockById(r.getSlotId()).orElse(null);
  if(slot==null||!slot.getAdvisorId().equals(id(a)))return ResponseEntity.status(403).build();
  if(!r.getStatus().equals("PENDING"))return ResponseEntity.badRequest().body(Map.of("error","This request has already been decided."));
  String d=in.decision().toUpperCase(Locale.ROOT);
  if(!d.equals("ACCEPT")&&!d.equals("DECLINE"))return ResponseEntity.badRequest().body(Map.of("error","Decision must be ACCEPT or DECLINE."));
  r.setStatus(d.equals("ACCEPT")?"ACCEPTED":"DECLINED"); slot.setStatus(d.equals("ACCEPT")?"BOOKED":"OPEN");
  requests.save(r);slots.save(slot);return ResponseEntity.ok(Map.of("status",r.getStatus()));
 }
}
