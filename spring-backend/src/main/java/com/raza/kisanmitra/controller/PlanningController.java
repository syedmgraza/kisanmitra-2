package com.raza.kisanmitra.controller;

import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.FarmerService;
import com.raza.kisanmitra.service.MlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Controller
public class PlanningController {
    private static final Logger log = LoggerFactory.getLogger(PlanningController.class);
    private static final List<String> SEASONS = List.of("Kharif", "Rabi", "Whole Year", "Zaid");
    private static final List<String> FERTILIZERS = List.of("Recommended", "Less than recommended", "More than recommended");

    private final MlClient ml;
    private final FarmerService farmers;

    public PlanningController(MlClient ml, FarmerService farmers) {
        this.ml = ml;
        this.farmers = farmers;
    }

    @GetMapping("/recommendation")
    String recommendationForm(Authentication auth, Model model) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        model.addAttribute("farmer", farmer);
        model.addAttribute("seasons", SEASONS);
        return "recommendation";
    }

    @PostMapping("/recommendation")
    String recommend(Authentication auth, @RequestParam String season, Model model) {
        season = canonicalSeason(season);
        log.info("Received crop recommendation form submission for season {}.", season);
        FarmerUser farmer = farmers.byEmail(auth.getName());
        model.addAttribute("farmer", farmer);
        model.addAttribute("seasons", SEASONS);
        model.addAttribute("selectedSeason", season);
        try {
            Map<?, ?> result = ml.recommend(Map.of(
                    "state", farmer.getState(),
                    "district", farmer.getDistrict(),
                    "season", season,
                    "land_size", farmer.getLandSizeHectares()));
            model.addAttribute("result", result);
        } catch (Exception e) {
            log.warn("Crop recommendation request to ML service failed.", e);
            model.addAttribute("serviceError", "Crop recommendation failed: " + errorDetail(e));
        }
        return "recommendation";
    }

    @GetMapping("/yield")
    String yieldForm(Authentication auth, Model model) {
        model.addAttribute("farmer", farmers.byEmail(auth.getName()));
        model.addAttribute("seasons", SEASONS);
        model.addAttribute("fertilizers", FERTILIZERS);
        return "yield";
    }

    @GetMapping("/yield/crops")
    @ResponseBody
    List<String> yieldCrops(Authentication auth, @RequestParam String season) {
        FarmerUser farmer = farmers.byEmail(auth.getName());
        return ml.yieldCrops(farmer.getState(), farmer.getDistrict(), canonicalSeason(season));
    }

    @PostMapping("/yield")
    String predictYield(Authentication auth,
                        @RequestParam String season,
                        @RequestParam String crop,
                        @RequestParam(defaultValue = "800") double rainfall,
                        @RequestParam(defaultValue = "Recommended") String fertilizer,
                        Model model) {
        season = canonicalSeason(season);
        crop = crop.trim();
        FarmerUser farmer = farmers.byEmail(auth.getName());
        model.addAttribute("farmer", farmer);
        model.addAttribute("seasons", SEASONS);
        model.addAttribute("fertilizers", FERTILIZERS);
        model.addAttribute("selectedSeason", season);
        model.addAttribute("crop", crop);
        model.addAttribute("rainfall", rainfall);
        model.addAttribute("fertilizer", fertilizer);
        try {
            model.addAttribute("result", ml.predictYield(Map.of(
                    "state", farmer.getState(),
                    "district", farmer.getDistrict(),
                    "season", season,
                    "crop", crop,
                    "rainfall", rainfall,
                    "fertilizer_choice", fertilizer,
                    "land_size", farmer.getLandSizeHectares())));
        } catch (Exception e) {
            log.warn("Yield prediction request to ML service failed.", e);
            model.addAttribute("serviceError", "Yield prediction failed: " + errorDetail(e));
        }
        return "yield";
    }

    @GetMapping("/weather")
    String weather(Authentication auth, Model model, @RequestParam(defaultValue = "Kharif") String season) {
        season = canonicalSeason(season);
        FarmerUser farmer = farmers.byEmail(auth.getName());
        model.addAttribute("farmer", farmer);
        model.addAttribute("seasons", SEASONS);
        model.addAttribute("selectedSeason", season);
        try {
            model.addAttribute("weather", ml.weather(farmer.getState(), season));
        } catch (Exception e) {
            log.warn("Weather request to ML service failed.", e);
            model.addAttribute("serviceError", "Weather details are temporarily unavailable: " + errorDetail(e));
        }
        return "weather";
    }

    private String errorDetail(Exception e) {
        String detail;
        if (e instanceof WebClientResponseException response) {
            String body = response.getResponseBodyAsString();
            detail = "HTTP " + response.getStatusCode().value() + (body == null || body.isBlank() ? "" : ": " + body);
        } else {
            detail = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
        }
        return detail.length() > 600 ? detail.substring(0, 600) + "…" : detail;
    }

    private String canonicalSeason(String requested) {
        String normalized = requested.trim();
        return SEASONS.stream().filter(season -> season.equalsIgnoreCase(normalized)).findFirst().orElse(normalized);
    }
}
