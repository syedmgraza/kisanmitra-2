package com.raza.kisanmitra.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class LocationCatalog {
    private final Map<String, List<String>> districtsByState;

    public LocationCatalog(ObjectMapper objectMapper) {
        try (var input = new ClassPathResource("static/data/districts.json").getInputStream()) {
            districtsByState = objectMapper.readValue(input, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load crop model location data.", e);
        }
    }

    public List<String> states() {
        return districtsByState.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    public List<String> districts(String state) {
        String canonicalState = canonicalState(state);
        return canonicalState == null ? List.of() : districtsByState.getOrDefault(canonicalState, List.of());
    }

    public String canonicalState(String state) {
        if (state == null) return null;
        return districtsByState.keySet().stream()
                .filter(value -> value.equalsIgnoreCase(state.trim()))
                .findFirst().orElse(null);
    }

    public String canonicalDistrict(String state, String district) {
        if (district == null) return null;
        return districts(state).stream()
                .filter(value -> value.equalsIgnoreCase(district.trim()))
                .findFirst().orElse(null);
    }
}
