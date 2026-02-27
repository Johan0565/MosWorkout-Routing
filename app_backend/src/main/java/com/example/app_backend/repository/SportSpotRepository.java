package com.example.app_backend.repository;

import com.example.app_backend.entity.SportSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportSpotRepository extends JpaRepository<SportSpot, Long> {

    // Твой текущий метод (оставляем, пригодится)
    @Query(value = """
            SELECT * FROM sport_spots 
            WHERE ST_DWithin(
                geom::geography, 
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 
                :radius
            )
            """, nativeQuery = true)
    List<SportSpot> findSpotsWithinRadius(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radiusInMeters
    );

    // НОВЫЙ МЕТОД: Поиск с фильтрацией по тренажерам
    @Query(value = """
            SELECT * FROM sport_spots 
            WHERE ST_DWithin(
                geom::geography, 
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, 
                :radius
            )
            -- Извлекаем текст из ключа raw_data и ищем в нем совпадение без учета регистра
            AND (services->>'raw_data') ILIKE '%' || :equipment || '%'
            """, nativeQuery = true)
    List<SportSpot> findSpotsWithinRadiusWithEquipment(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radiusInMeters,
            @Param("equipment") String equipment
    );
}