package com.gruppeM.energy_rest_api.repository;

import com.gruppeM.energy_rest_api.model.HourlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HourlyUsageRepository
        extends JpaRepository<HourlyUsage, Instant> {

    List<HourlyUsage> findAllByHourKeyBetween(Instant from, Instant to);
}
