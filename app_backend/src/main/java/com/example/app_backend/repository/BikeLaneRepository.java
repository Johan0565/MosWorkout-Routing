package com.example.app_backend.repository;

import com.example.app_backend.entity.BikeLane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikeLaneRepository extends JpaRepository<BikeLane, Long> {

    // Ищем велодорожки, которые пересекают заданный радиус вокруг пользователя
    @Query(value = """
            SELECT * FROM bike_lanes 
            WHERE ST_DWithin(
                geom::geography, 
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 
                :radius
            )
            """, nativeQuery = true)
    List<BikeLane> findLanesWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radiusInMeters
    );
}