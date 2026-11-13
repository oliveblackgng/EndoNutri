package com.endonutri.auth;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="app_users")
public class AppUser {
 @Id private UUID id;
 @Column(name="full_name",nullable=false) private String fullName;
 @Column(nullable=false,unique=true) private String email;
 @Column(name="password_hash",nullable=false) private String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(nullable=false) private boolean enabled=true;
 protected AppUser() {}
 public AppUser(String name,String email,String hash,Role role){this.id=UUID.randomUUID();this.fullName=name;this.email=email.toLowerCase();this.passwordHash=hash;this.role=role;this.createdAt=Instant.now();}
 public UUID getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;}
 public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;} public boolean isEnabled(){return enabled;}
}
