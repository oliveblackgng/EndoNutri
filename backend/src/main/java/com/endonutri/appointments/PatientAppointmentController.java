package com.endonutri.appointments;

import com.endonutri.auth.AppUser;
import com.endonutri.auth.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/patient/appointments")
public class PatientAppointmentController {
 private final AdvisorSlotRepository slots; private final AppointmentRequestRepository requests; private final UserRepository users;
 public PatientAppointmentController(AdvisorSlotRepository slots,AppointmentRequestRepository requests,UserRepository users){this.slots=slots;this.requests=requests;this.users=users;}
 private UUID id(Authentication a){return ((AppUser)a.getPrincipal()).getId();}
 public record RequestInput(@Size(max=500) String message){}
 @GetMapping("/availability") public List<Map<String,Object>> availability(){
  List<Map<String,Object>> out=new ArrayList<>();
  for(AdvisorSlot s:slots.findByStatusAndStartsAtAfterOrderByStartsAtAsc("OPEN",Instant.now())){
   AppUser advisor=users.findById(s.getAdvisorId()).orElse(null);
   if(advisor!=null)out.add(Map.of("slotId",s.getId(),"advisorName",advisor.getFullName(),"startsAt",s.getStartsAt(),"endsAt",s.getEndsAt()));
  } return out;
 }
 @GetMapping public List<Map<String,Object>> mine(Authentication a){
  List<Map<String,Object>> out=new ArrayList<>();
  for(AppointmentRequest r:requests.findByPatientIdOrderByCreatedAtDesc(id(a))){
   AdvisorSlot s=slots.findById(r.getSlotId()).orElse(null);AppUser advisor=s==null?null:users.findById(s.getAdvisorId()).orElse(null);
   if(s!=null)out.add(Map.of("id",r.getId(),"status",r.getStatus(),"advisorName",advisor==null?"Advisor":advisor.getFullName(),"startsAt",s.getStartsAt(),"endsAt",s.getEndsAt(),"message",Objects.toString(r.getPatientMessage(),"")));
  } return out;
 }
 @PostMapping("/{slotId}") @Transactional
 public ResponseEntity<?> request(@PathVariable UUID slotId,@RequestBody(required=false) RequestInput in,Authentication a){
  AdvisorSlot slot=slots.lockById(slotId).orElse(null);
  if(slot==null||!slot.getStatus().equals("OPEN")||!slot.getStartsAt().isAfter(Instant.now()))return ResponseEntity.status(409).body(Map.of("error","That slot is no longer available. Please choose another."));
  slot.setStatus("HELD");slots.save(slot);
  AppointmentRequest req=requests.save(new AppointmentRequest(slotId,id(a),in==null?null:in.message()));
  return ResponseEntity.status(201).body(Map.of("id",req.getId(),"status",req.getStatus()));
 }
}
