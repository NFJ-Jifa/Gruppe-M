package com.gruppeM.energy_rest_api.repository;
import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface CurrentPercentageRepository
        extends JpaRepository<CurrentPercentage, Instant> {
    // JpaRepository::findById(Instant) нам подходит
}
