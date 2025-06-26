package com.gruppeM.energy_rest_api.repository;

import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Repository interface for accessing the 'current_percentage' table.
 * Inherits basic CRUD methods from JpaRepository.
 */
@Repository
public interface CurrentPercentageRepository
        extends JpaRepository<CurrentPercentage, Instant> {

    // No custom methods are needed here.
    // The default method findById(Instant hourKey) is sufficient to retrieve records by timestamp.

}
