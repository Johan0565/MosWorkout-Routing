package com.example.app_backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BikeLaneDto {
    private Long id;
    private String name;
    private BigDecimal width;
    private String type;

    // Двумерный массив: список линий, внутри которых лежат точки [Широта, Долгота]
    private List<List<double[]>> coordinates;
}