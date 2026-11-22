package com.raza.kisanmitra.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crop_plans")
public class CropPlan {
    public enum Status { ACTIVE, HARVESTED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long ownerUserId;
    @Column(nullable = false, length = 60) private String cropName;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal areaAcres;
    @Column(nullable = false, length = 30) private String season;
    @Column(nullable = false) private LocalDate sowingDate;
    @Column(nullable = false, length = 80) private String district;
    @Column(nullable = false, length = 80) private String state;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "cropPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CropPlanTask> tasks = new ArrayList<>();

    protected CropPlan() { }

    public CropPlan(Long ownerUserId, String cropName, BigDecimal areaAcres, String season,
                    LocalDate sowingDate, String district, String state) {
        this.ownerUserId = ownerUserId;
        this.cropName = cropName;
        this.areaAcres = areaAcres;
        this.season = season;
        this.sowingDate = sowingDate;
        this.district = district;
        this.state = state;
    }

    public void addTask(CropPlanTask task) { tasks.add(task); task.setCropPlan(this); }
    public Long getId() { return id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public String getCropName() { return cropName; }
    public BigDecimal getAreaAcres() { return areaAcres; }
    public String getSeason() { return season; }
    public LocalDate getSowingDate() { return sowingDate; }
    public String getDistrict() { return district; }
    public String getState() { return state; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<CropPlanTask> getTasks() { return tasks; }
    public long getResolvedTaskCount() {
        return tasks.stream().filter(task -> task.getStatus() == CropPlanTask.Status.DONE
                || task.getStatus() == CropPlanTask.Status.SKIPPED).count();
    }
    public int getProgressPercent() {
        return tasks.isEmpty() ? 0 : (int) Math.round(getResolvedTaskCount() * 100.0 / tasks.size());
    }
}
