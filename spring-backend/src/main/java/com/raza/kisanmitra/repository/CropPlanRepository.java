package com.raza.kisanmitra.repository;

import com.raza.kisanmitra.model.CropPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropPlanRepository extends JpaRepository<CropPlan, Long> {
    @EntityGraph(attributePaths = "tasks")
    List<CropPlan> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    @EntityGraph(attributePaths = "tasks")
    Optional<CropPlan> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
