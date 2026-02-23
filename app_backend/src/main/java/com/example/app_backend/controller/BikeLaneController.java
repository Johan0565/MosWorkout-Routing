package com.example.app_backend.controller;

import com.example.app_backend.dto.BikeLaneDto;
import com.example.app_backend.entity.BikeLane;
import com.example.app_backend.repository.BikeLaneRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bikelanes")
@RequiredArgsConstructor
public class BikeLaneController {

    private final BikeLaneRepository bikeLaneRepository;

    @GetMapping("/nearby")
    public List<BikeLaneDto> getNearbyLanes(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "3000") double radius) {

        // 1. Достаем дорожки из БД в заданном радиусе
        List<BikeLane> lanes = bikeLaneRepository.findLanesWithinRadius(lat, lon, radius);

        // 2. Упаковываем в DTO для отправки на телефон
        return lanes.stream().map(lane -> {
            BikeLaneDto dto = new BikeLaneDto();
            dto.setId(Long.valueOf(lane.getGlobalId()));
            dto.setName(lane.getName());
            dto.setWidth(lane.getLaneWidth());
            dto.setType(lane.getLaneType());

            // 3. Распаковка MultiLineString
            List<List<double[]>> allLines = new ArrayList<>();
            if (lane.getGeom() != null) {
                // Идем по каждой отдельной линии в маршруте
                for (int i = 0; i < lane.getGeom().getNumGeometries(); i++) {
                    Geometry geometry = lane.getGeom().getGeometryN(i);
                    List<double[]> singleLineCoords = new ArrayList<>();

                    // Собираем точки текущей линии
                    for (Coordinate coord : geometry.getCoordinates()) {
                        // Flutter ожидает формат [Широта(Y), Долгота(X)]
                        singleLineCoords.add(new double[]{coord.y, coord.x});
                    }
                    allLines.add(singleLineCoords);
                }
            }
            dto.setCoordinates(allLines);
            return dto;
        }).collect(Collectors.toList());
    }
}