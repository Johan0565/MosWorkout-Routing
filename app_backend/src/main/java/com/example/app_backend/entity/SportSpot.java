package com.example.app_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

@Entity
@Table(name = "sport_spots")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class SportSpot {

    @Id
    @Column(name = "global_id")
    private Long id;

    @Column(name = "dataset_id", nullable = false)
    private Long datasetId;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "district")
    private String district;

    @Column(name = "surface_type")
    private String surfaceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "services", columnDefinition = "jsonb")
    private Map<String, Object> services;




    public void setGlobalId(long count) {
        this.id = count;
    }

    public String getGlobalId() {
        return String.valueOf(id);
    }
    @JsonIgnore // <--- Прячем сложный объект от генератора JSON
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;

    // Секретный трюк: Jackson увидит геттеры и сам создаст поля "lat" и "lon" в JSON
    public Double getLat() {
        return geom != null ? geom.getY() : null;
    }

    public Double getLon() {
        return geom != null ? geom.getX() : null;
    }
}
