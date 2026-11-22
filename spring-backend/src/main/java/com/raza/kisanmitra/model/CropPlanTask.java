package com.raza.kisanmitra.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "crop_plan_tasks")
public class CropPlanTask {
    public enum Status { UPCOMING, DONE, SKIPPED, DELAYED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_plan_id", nullable = false)
    private CropPlan cropPlan;
    @Column(name = "task_position", nullable = false) private int position;
    @Column(nullable = false, length = 100) private String title;
    @Column(nullable = false, length = 500) private String guidance;
    @Column(nullable = false) private LocalDate suggestedDate;
    @Column(nullable = false) private LocalDate scheduledDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status = Status.UPCOMING;
    private LocalDate completedDate;
    @Size(max = 500) @Column(length = 500) private String note;

    protected CropPlanTask() { }

    public CropPlanTask(int position, String title, String guidance, LocalDate suggestedDate) {
        this.position = position;
        this.title = title;
        this.guidance = guidance;
        this.suggestedDate = suggestedDate;
        this.scheduledDate = suggestedDate;
    }

    void setCropPlan(CropPlan cropPlan) { this.cropPlan = cropPlan; }
    public Long getId() { return id; }
    public CropPlan getCropPlan() { return cropPlan; }
    public int getPosition() { return position; }
    public String getTitle() { return title; }
    public String getGuidance() { return guidance; }
    public LocalDate getSuggestedDate() { return suggestedDate; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public Status getStatus() { return status; }
    public LocalDate getCompletedDate() { return completedDate; }
    public String getNote() { return note; }

    public void markDone(LocalDate date, String note) {
        status = Status.DONE;
        completedDate = date;
        this.note = note;
    }

    public void markSkipped(String note) {
        status = Status.SKIPPED;
        completedDate = null;
        this.note = note;
    }

    public void delayUntil(LocalDate date, String note) {
        scheduledDate = date;
        status = Status.DELAYED;
        this.note = note;
    }
}
