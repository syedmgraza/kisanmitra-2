package com.raza.kisanmitra.controller;

import com.raza.kisanmitra.model.CropPlan;
import com.raza.kisanmitra.model.CropPlanActivity;
import com.raza.kisanmitra.model.CropPlanTask;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.repository.CropPlanRepository;
import com.raza.kisanmitra.repository.CropPlanActivityRepository;
import com.raza.kisanmitra.service.CropPlanCatalog;
import com.raza.kisanmitra.service.FarmerService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CropPlanController {
    private static final BigDecimal HECTARES_PER_ACRE = new BigDecimal("0.40468564224");
    private static final List<String> SEASONS = List.of("Kharif", "Rabi", "Whole Year", "Zaid");

    private final CropPlanRepository plans;
    private final CropPlanActivityRepository activities;
    private final FarmerService farmers;
    private final CropPlanCatalog catalog;

    public CropPlanController(CropPlanRepository plans, CropPlanActivityRepository activities,
                              FarmerService farmers, CropPlanCatalog catalog) {
        this.plans = plans;
        this.activities = activities;
        this.farmers = farmers;
        this.catalog = catalog;
    }

    @GetMapping("/plan")
    String tracker(Authentication auth, Model model) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        List<CropPlan> farmerPlans = plans.findByOwnerUserIdOrderByCreatedAtDesc(farmer.getId());
        Map<Long, List<CropPlanActivity>> activityMap = new HashMap<>();
        if (!farmerPlans.isEmpty()) {
            List<Long> planIds = farmerPlans.stream().map(CropPlan::getId).toList();
            activities.findByCropPlan_IdInOrderByActivityDateDesc(planIds).forEach(activity ->
                    activityMap.computeIfAbsent(activity.getCropPlan().getId(), ignored -> new ArrayList<>()).add(activity));
        }
        BigDecimal totalAcres = BigDecimal.valueOf(farmer.getLandSizeHectares())
                .divide(HECTARES_PER_ACRE, 2, RoundingMode.DOWN);
        BigDecimal allocatedAcres = farmerPlans.stream()
                .filter(plan -> plan.getStatus() == CropPlan.Status.ACTIVE)
                .map(CropPlan::getAreaAcres).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("farmer", farmer);
        model.addAttribute("cropPlans", farmerPlans);
        model.addAttribute("activitiesByPlan", activityMap);
        model.addAttribute("cropOptions", catalog.getCrops());
        model.addAttribute("seasons", SEASONS);
        model.addAttribute("totalAcres", totalAcres);
        model.addAttribute("allocatedAcres", allocatedAcres);
        BigDecimal remainingAcres = totalAcres.subtract(allocatedAcres).max(BigDecimal.ZERO);
        model.addAttribute("remainingAcres", remainingAcres);
        model.addAttribute("hasAvailableAcres", remainingAcres.signum() > 0);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Kolkata")));
        return "crop-plans";
    }

    @PostMapping("/plan")
    String create(Authentication auth,
                  @RequestParam String crop,
                  @RequestParam String season,
                  @RequestParam BigDecimal areaAcres,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sowingDate,
                  RedirectAttributes redirect) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        CropPlanCatalog.CropOption option = catalog.getCrops().stream()
                .filter(candidate -> candidate.getName().equalsIgnoreCase(crop.trim())).findFirst().orElse(null);
        String canonicalSeason = SEASONS.stream().filter(value -> value.equalsIgnoreCase(season.trim())).findFirst().orElse(null);
        if (option == null || canonicalSeason == null) {
            redirect.addFlashAttribute("planError", "Choose a crop and season from the available lists.");
            return "redirect:/plan#new-plan";
        }
        if (areaAcres == null || areaAcres.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("planError", "Enter a crop area greater than zero acres.");
            return "redirect:/plan#new-plan";
        }

        List<CropPlan> farmerPlans = plans.findByOwnerUserIdOrderByCreatedAtDesc(farmer.getId());
        BigDecimal allocatedAcres = farmerPlans.stream()
                .filter(plan -> plan.getStatus() == CropPlan.Status.ACTIVE)
                .map(CropPlan::getAreaAcres).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAcres = BigDecimal.valueOf(farmer.getLandSizeHectares())
                .divide(HECTARES_PER_ACRE, 2, RoundingMode.DOWN);
        if (areaAcres.scale() > 2) {
            redirect.addFlashAttribute("planError", "Enter crop area using at most two decimal places.");
            return "redirect:/plan#new-plan";
        }
        if (areaAcres.compareTo(totalAcres.subtract(allocatedAcres)) > 0) {
            redirect.addFlashAttribute("planError", "That area exceeds the remaining "
                    + totalAcres.subtract(allocatedAcres).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
                    + " acres. Adjust this crop area or finish another crop plan first.");
            return "redirect:/plan#new-plan";
        }

        CropPlan plan = new CropPlan(farmer.getId(), option.getName(), areaAcres, canonicalSeason,
                sowingDate, farmer.getDistrict(), farmer.getState());
        catalog.addTasks(plan);
        plans.save(plan);
        redirect.addFlashAttribute("planNotice", option.getName() + " plan added. You can track each field task below.");
        return "redirect:/plan#plan-" + plan.getId();
    }

    @PostMapping("/plan/{planId}/tasks/{taskId}")
    String updateTask(Authentication auth, @PathVariable Long planId, @PathVariable Long taskId,
                      @RequestParam String action,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate,
                      @RequestParam(defaultValue = "") String note,
                      RedirectAttributes redirect) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        CropPlan plan = ownedPlan(planId, farmer.getId());
        if (plan.getStatus() != CropPlan.Status.ACTIVE) {
            redirect.addFlashAttribute("planError", "This crop plan is already closed.");
            return "redirect:/plan#plan-" + planId;
        }
        CropPlanTask task = plan.getTasks().stream().filter(item -> item.getId().equals(taskId)).findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        String safeNote = note.trim().substring(0, Math.min(note.trim().length(), 500));
        switch (action.toLowerCase()) {
            case "done" -> task.markDone(LocalDate.now(ZoneId.of("Asia/Kolkata")), safeNote);
            case "skip" -> task.markSkipped(safeNote);
            case "delay" -> {
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                if (scheduledDate == null || !scheduledDate.isAfter(task.getScheduledDate())
                        || !scheduledDate.isAfter(today)) {
                    redirect.addFlashAttribute("planError", "Choose a future date later than the current reminder.");
                    return "redirect:/plan#plan-" + planId;
                }
                task.delayUntil(scheduledDate, safeNote);
            }
            default -> {
                redirect.addFlashAttribute("planError", "Choose Done, Skip, or Delay for the task.");
                return "redirect:/plan#plan-" + planId;
            }
        }
        plans.save(plan);
        return "redirect:/plan#plan-" + planId;
    }

    @PostMapping("/plan/{planId}/activities")
    String addActivity(Authentication auth, @PathVariable Long planId,
                       @RequestParam String activityType,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDate,
                       @RequestParam(required = false) BigDecimal rainfallMm,
                       @RequestParam(defaultValue = "") String inputName,
                       @RequestParam(defaultValue = "") String inputAmount,
                       @RequestParam(defaultValue = "") String note,
                       RedirectAttributes redirect) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        CropPlan plan = ownedPlan(planId, farmer.getId());
        if (plan.getStatus() != CropPlan.Status.ACTIVE) {
            redirect.addFlashAttribute("planError", "This crop plan is already closed.");
            return "redirect:/plan#plan-" + planId;
        }

        CropPlanActivity.Type type;
        try {
            type = CropPlanActivity.Type.valueOf(activityType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("planError", "Choose one of the available field log types.");
            return "redirect:/plan#plan-" + planId;
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        if (activityDate.isAfter(today)) {
            redirect.addFlashAttribute("planError", "Field logs must use today or an earlier date.");
            return "redirect:/plan#plan-" + planId;
        }
        if (type == CropPlanActivity.Type.RAINFALL && (rainfallMm == null || rainfallMm.signum() < 0)) {
            redirect.addFlashAttribute("planError", "Enter the rainfall amount in millimeters.");
            return "redirect:/plan#plan-" + planId;
        }
        String safeName = inputName.trim();
        if ((type == CropPlanActivity.Type.FERTILIZER || type == CropPlanActivity.Type.PESTICIDE) && safeName.isBlank()) {
            redirect.addFlashAttribute("planError", "Enter the fertilizer or pesticide name you used.");
            return "redirect:/plan#plan-" + planId;
        }

        String safeAmount = inputAmount.trim();
        String safeNote = note.trim();
        activities.save(new CropPlanActivity(plan, type, activityDate,
                type == CropPlanActivity.Type.RAINFALL ? rainfallMm : null,
                safeName.isBlank() ? null : safeName.substring(0, Math.min(safeName.length(), 100)),
                safeAmount.isBlank() ? null : safeAmount.substring(0, Math.min(safeAmount.length(), 80)),
                safeNote.isBlank() ? null : safeNote.substring(0, Math.min(safeNote.length(), 500))));
        redirect.addFlashAttribute("planNotice", "Field log added to " + plan.getCropName() + ".");
        return "redirect:/plan#plan-" + planId;
    }

    @PostMapping("/plan/{planId}/close")
    String closePlan(Authentication auth, @PathVariable Long planId, @RequestParam String outcome,
                     RedirectAttributes redirect) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        CropPlan plan = ownedPlan(planId, farmer.getId());
        if ("harvested".equalsIgnoreCase(outcome)) plan.setStatus(CropPlan.Status.HARVESTED);
        else if ("cancelled".equalsIgnoreCase(outcome)) plan.setStatus(CropPlan.Status.CANCELLED);
        else {
            redirect.addFlashAttribute("planError", "Choose a valid way to close this plan.");
            return "redirect:/plan#plan-" + planId;
        }
        plans.save(plan);
        redirect.addFlashAttribute("planNotice", "Crop plan closed. Its acreage is available for another plan.");
        return "redirect:/plan";
    }

    private CropPlan ownedPlan(Long planId, Long ownerId) {
        return plans.findByIdAndOwnerUserId(planId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("Crop plan not found for this farmer."));
    }
}
