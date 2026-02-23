package com.example.app_backend.repository;

import com.example.app_backend.entity.BikeLane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BikeLaneRepository extends JpaRepository<BikeLane, Long> {
    //Base functional
}
