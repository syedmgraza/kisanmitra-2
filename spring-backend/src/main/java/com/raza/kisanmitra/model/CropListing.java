package com.raza.kisanmitra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity @Table(name="crop_listings")
public class CropListing {
    public enum Status { ACTIVE, SOLD }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @NotBlank private String sellerName;
    @NotBlank private String sellerContact;
    @NotBlank private String cropName;
    @Positive private double quantityKg;
    @Positive private double pricePerKg;
    @NotBlank private String locationState;
    @NotBlank private String locationDistrict;
    @Size(max=1200) private String description;
    private String imagePath;
    @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.ACTIVE;
    @Column(nullable=false) private Long ownerUserId;
    public Long getId(){return id;} public String getSellerName(){return sellerName;} public void setSellerName(String v){sellerName=v;}
    public String getCropImageSlug(){
        if(cropName==null||cropName.isBlank()) return "crop-placeholder";
        return cropName.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+","-").replaceAll("^-|-$","");
    }
    public String getSellerContact(){return sellerContact;} public void setSellerContact(String v){sellerContact=v;} public String getCropName(){return cropName;} public void setCropName(String v){cropName=v;}
    public double getQuantityKg(){return quantityKg;} public void setQuantityKg(double v){quantityKg=v;} public double getPricePerKg(){return pricePerKg;} public void setPricePerKg(double v){pricePerKg=v;}
    public String getLocationState(){return locationState;} public void setLocationState(String v){locationState=v;} public String getLocationDistrict(){return locationDistrict;} public void setLocationDistrict(String v){locationDistrict=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getImagePath(){return imagePath;} public void setImagePath(String v){imagePath=v;}
    public Instant getCreatedAt(){return createdAt;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public Long getOwnerUserId(){return ownerUserId;} public void setOwnerUserId(Long v){ownerUserId=v;}
}
