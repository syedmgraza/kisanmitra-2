package com.raza.kisanmitra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity @Table(name="farmers", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class FarmerUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @NotBlank @Size(max=80) private String username;
    @Email @NotBlank @Column(nullable=false, unique=true) private String email;
    @NotBlank @Size(min=8,max=100) @Column(nullable=false) private String passwordHash;
    @NotBlank private String state;
    @NotBlank private String district;
    @Positive private double landSizeHectares;
    @Column(nullable=false, updatable=false) private Instant createdAt=Instant.now();
    public Long getId(){return id;} public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v==null?null:v.trim().toLowerCase();}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public String getState(){return state;} public void setState(String v){state=v;} public String getDistrict(){return district;} public void setDistrict(String v){district=v;}
    public double getLandSizeHectares(){return landSizeHectares;} public void setLandSizeHectares(double v){landSizeHectares=v;}
    public Instant getCreatedAt(){return createdAt;}
}
