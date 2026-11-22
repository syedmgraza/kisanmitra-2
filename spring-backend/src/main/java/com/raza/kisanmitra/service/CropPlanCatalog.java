package com.raza.kisanmitra.service;

import com.raza.kisanmitra.model.CropPlan;
import com.raza.kisanmitra.model.CropPlanTask;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** Starter checklists for high-coverage crops in the included yield dataset. */
@Service
public class CropPlanCatalog {
    public static class CropOption {
        private final String name;
        private final int typicalDaysToHarvest;

        public CropOption(String name, int typicalDaysToHarvest) {
            this.name = name;
            this.typicalDaysToHarvest = typicalDaysToHarvest;
        }

        public String getName() { return name; }
        public int getTypicalDaysToHarvest() { return typicalDaysToHarvest; }
    }
    private record TaskSpec(String title, String guidance, int dayOffset) { }

    private static final List<CropOption> CROPS = List.of(
            new CropOption("Rice", 120),
            new CropOption("Wheat", 120),
            new CropOption("Maize", 100),
            new CropOption("Groundnut", 112),
            new CropOption("Gram", 110)
    );

    private static final List<TaskSpec> TASKS = List.of(
            new TaskSpec("Prepare the field", "Prepare soil and arrange seed or planting material using local guidance.", -7),
            new TaskSpec("Sowing or planting", "Record the actual planting date and variety in your field notes.", 0),
            new TaskSpec("Check crop establishment", "Check germination or transplant establishment and note any gaps.", 10),
            new TaskSpec("Irrigation check 1", "Check soil moisture, recent rain, and crop need before irrigating.", 18),
            new TaskSpec("Weed and nutrient check", "Inspect weeds and crop condition. Use fertilizer only as advised for your field.", 32),
            new TaskSpec("Irrigation check 2", "Review soil moisture and rainfall; adjust this reminder to your local conditions.", 48),
            new TaskSpec("Pest and disease scouting", "Inspect plants and record symptoms. Confirm any treatment with local advice.", 62),
            new TaskSpec("Irrigation check 3", "Check crop and soil before watering; skip if rain or field conditions make it unnecessary.", 78),
            new TaskSpec("Pre-harvest field check", "Check crop maturity and local harvest guidance before arranging labor or storage.", -14),
            new TaskSpec("Harvest readiness", "Use crop maturity and local conditions to confirm the harvest date.", 0)
    );

    public List<CropOption> getCrops() { return CROPS; }

    public void addTasks(CropPlan plan) {
        int harvestDays = CROPS.stream().filter(c -> c.getName().equals(plan.getCropName()))
                .map(CropOption::getTypicalDaysToHarvest).findFirst().orElse(110);
        for (int i = 0; i < TASKS.size(); i++) {
            TaskSpec spec = TASKS.get(i);
            int offset = switch (spec.title()) {
                case "Pre-harvest field check" -> harvestDays - 14;
                case "Harvest readiness" -> harvestDays;
                default -> spec.dayOffset();
            };
            plan.addTask(new CropPlanTask(i + 1, spec.title(), spec.guidance(), plan.getSowingDate().plusDays(offset)));
        }
    }
}
