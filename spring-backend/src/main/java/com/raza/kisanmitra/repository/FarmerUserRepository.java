package com.raza.kisanmitra.repository;
import com.raza.kisanmitra.model.FarmerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface FarmerUserRepository extends JpaRepository<FarmerUser,Long> { Optional<FarmerUser> findByEmail(String email); }
