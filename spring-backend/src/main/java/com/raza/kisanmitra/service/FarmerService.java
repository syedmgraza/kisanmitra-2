package com.raza.kisanmitra.service;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.repository.FarmerUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service public class FarmerService {
 private final FarmerUserRepository users; private final PasswordEncoder encoder;
 public FarmerService(FarmerUserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public FarmerUser register(FarmerUser user){if(users.findByEmail(user.getEmail()).isPresent())throw new IllegalArgumentException("An account with this email already exists."); user.setPasswordHash(encoder.encode(user.getPasswordHash()));return users.save(user);}
 public FarmerUser byEmail(String email){return users.findByEmail(email.toLowerCase()).orElseThrow();}
}
