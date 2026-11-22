package com.raza.kisanmitra.repository;

import com.raza.kisanmitra.model.CropPlanActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropPlanActivityRepository extends JpaRepository<CropPlanActivity, Long> {
    List<CropPlanActivity> findByCropPlan_IdInOrderByActivityDateDesc(List<Long> cropPlanIds);
}
