package com.endonutri.security;
import com.endonutri.auth.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key; private final long expiry;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiry){
  if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes");
  this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiry=expiry;
 }
 public String issue(AppUser u){long now=System.currentTimeMillis();return Jwts.builder().subject(u.getId().toString()).claim("role",u.getRole().name()).issuedAt(new Date(now)).expiration(new Date(now+expiry)).signWith(key).compact();}
 public String subject(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public String role(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role",String.class);}
}
