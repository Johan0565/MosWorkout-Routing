package com.example.app_backend.controller;

import com.example.app_backend.entity.SportSpot;
import com.example.app_backend.repository.SportSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SportSpotController {

    private final SportSpotRepository sportSpotRepository;

    @GetMapping("/search")
    public List<SportSpot> searchSpots(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "2000") double radius,
            @RequestParam(required = false) String equipment) {

        // Если пользователь выбрал конкретный тренажер (например, "турник")
        if (equipment != null && !equipment.isBlank()) {
            return sportSpotRepository.findSpotsWithinRadiusWithEquipment(lat, lon, radius, equipment);
        }

        // Если ничего не выбрал — отдаем просто все площадки поблизости
        return sportSpotRepository.findSpotsWithinRadius(lat, lon, radius);
    }
}