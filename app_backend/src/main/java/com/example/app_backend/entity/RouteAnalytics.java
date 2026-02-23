package com.example.app_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RouteAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_lat", nullable = false)
    private Double searchLat;

    @Column(name = "search_lon", nullable = false)
    private Double searchLon;

    @Column(name = "requested_distance")
    private Double requestedDistance;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
