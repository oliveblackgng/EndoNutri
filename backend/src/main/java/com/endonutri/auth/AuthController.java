package com.endonutri.auth;
import com.endonutri.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
 public record RegisterRequest(@NotBlank @Size(max=140) String fullName,@Email @NotBlank String email,
   @NotBlank @Size(min=12,max=72) String password,@NotNull Role role) {}
 public record LoginRequest(@Email @NotBlank String email,@NotBlank String password) {}
 @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){
  if(r.role()==Role.ADMIN) return ResponseEntity.status(403).body(Map.of("error","Administrator accounts are provisioned by operations."));
  if(users.findByEmail(r.email().toLowerCase()).isPresent()) return ResponseEntity.badRequest().body(Map.of("error","Email is already registered."));
  AppUser u=users.save(new AppUser(r.fullName(),r.email(),encoder.encode(r.password()),r.role()));
  return ResponseEntity.status(201).body(Map.of("token",jwt.issue(u),"role",u.getRole(),"name",u.getFullName()));
 }
 @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
  AppUser u=users.findByEmail(r.email().toLowerCase()).orElse(null);
  if(u==null || !encoder.matches(r.password(),u.getPasswordHash()) || !u.isEnabled()) return ResponseEntity.status(401).body(Map.of("error","Invalid email or password."));
  return ResponseEntity.ok(Map.of("token",jwt.issue(u),"role",u.getRole(),"name",u.getFullName()));
 }
}
