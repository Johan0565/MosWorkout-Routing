package com.example.app_backend.repository;

import com.example.app_backend.entity.RouteAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteAnalyticsRepository extends JpaRepository<RouteAnalytics, Long> {
}