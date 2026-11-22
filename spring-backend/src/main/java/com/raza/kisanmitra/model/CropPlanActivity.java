package com.raza.kisanmitra.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crop_plan_activities")
public class CropPlanActivity {
    public enum Type { RAINFALL, IRRIGATION, FERTILIZER, PESTICIDE, FIELD_NOTE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_plan_id", nullable = false)
    private CropPlan cropPlan;
    @Enumerated(EnumType.STRING) @Column(name = "activity_type", nullable = false, length = 20)
    private Type type;
    @Column(nullable = false) private LocalDate activityDate;
    @Column(precision = 7, scale = 2) private BigDecimal rainfallMm;
    @Column(length = 100) private String inputName;
    @Column(length = 80) private String inputAmount;
    @Column(length = 500) private String note;

    protected CropPlanActivity() { }

    public CropPlanActivity(CropPlan cropPlan, Type type, LocalDate activityDate,
                            BigDecimal rainfallMm, String inputName, String inputAmount, String note) {
        this.cropPlan = cropPlan;
        this.type = type;
        this.activityDate = activityDate;
        this.rainfallMm = rainfallMm;
        this.inputName = inputName;
        this.inputAmount = inputAmount;
        this.note = note;
    }

    public Long getId() { return id; }
    public CropPlan getCropPlan() { return cropPlan; }
    public Type getType() { return type; }
    public LocalDate getActivityDate() { return activityDate; }
    public BigDecimal getRainfallMm() { return rainfallMm; }
    public String getInputName() { return inputName; }
    public String getInputAmount() { return inputAmount; }
    public String getNote() { return note; }
}
