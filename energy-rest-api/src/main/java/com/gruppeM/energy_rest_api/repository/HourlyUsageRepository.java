package com.gruppeM.energy_rest_api.repository;


import com.gruppeM.energy_rest_api.model.HourlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, Instant> {

    // 2.1. Select all records between start and end hours (inclusive)
    List<HourlyUsage> findAllByHourKeyBetween(Instant start, Instant end);

    // 3.1. Find the minimum hourKey
    @Query("SELECT MIN(h.hourKey) FROM HourlyUsage h")
    Optional<Instant> findMinHourKey();

    // 3.2. Find the maximum hourKey
    @Query("SELECT MAX(h.hourKey) FROM HourlyUsage h")
    Optional<Instant> findMaxHourKey();

    // For current-endpoint (alternative to findAll + max)
    HourlyUsage findTopByOrderByHourKeyDesc();
}
