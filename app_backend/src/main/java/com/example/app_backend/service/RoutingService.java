package com.example.app_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RoutingService {

    private final RestTemplate restTemplate;

    public RoutingService() {
        this.restTemplate = new RestTemplate();
    }

    // Метод для построения пешеходного маршрута (бег) между двумя точками
    public String getRoute(double startLat, double startLon, double endLat, double endLon) {
        try {
            // Формируем URL для бесплатного сервера OSRM
            // Внимание: OSRM принимает координаты в формате Долгота,Широта (lon,lat)
            String url = String.format(
                    "http://router.project-osrm.org/route/v1/foot/%s,%s;%s,%s?overview=full&geometries=geojson",
                    startLon, startLat, endLon, endLat
            );

            log.info("Запрашиваем маршрут у OSRM: {}", url);

            // Делаем HTTP-запрос к навигатором и получаем ответ в формате JSON (строка)
            String response = restTemplate.getForObject(url, String.class);
            return response;

        } catch (Exception e) {
            log.error("Ошибка при построении маршрута: ", e);
            return null;
        }
    }
}