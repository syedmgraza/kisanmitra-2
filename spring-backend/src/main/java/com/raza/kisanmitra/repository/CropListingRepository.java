package com.raza.kisanmitra.repository;
import com.raza.kisanmitra.model.CropListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CropListingRepository extends JpaRepository<CropListing,Long> {
 List<CropListing> findByStatusAndLocationDistrictIgnoreCaseOrderByCreatedAtDesc(CropListing.Status status,String district);
 List<CropListing> findByStatusOrderByCreatedAtDesc(CropListing.Status status);
 List<CropListing> findByStatusAndCropNameContainingIgnoreCaseOrderByCreatedAtDesc(CropListing.Status status,String cropName);
 List<CropListing> findByStatusAndLocationDistrictIgnoreCaseAndCropNameContainingIgnoreCaseOrderByCreatedAtDesc(CropListing.Status status,String district,String cropName);
}
