package com.example.app_backend.controller;

import com.example.app_backend.service.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RoutingService routingService;

    // Сюда телефон передает: где стоим мы (start) и куда бежим (end)
    @GetMapping("/build")
    public String buildRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon) {

        // Вызываем наш сервис навигации
        return routingService.getRoute(startLat, startLon, endLat, endLon);
    }
}