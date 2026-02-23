package com.example.app_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.MultiLineString;
import jakarta.persistence.Column;

import java.math.BigDecimal;

@Entity
@Table(name = "bike_lanes")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class BikeLane {
    @Id
    @Column(name = "global_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "lane_width", precision = 5, scale = 2)
    private BigDecimal laneWidth;

    @Column(name = "lane_type")
    private String laneType;

    @Column(name = "geom", columnDefinition = "geometry(MultiLineString,4326)")
    private MultiLineString geom;
}
