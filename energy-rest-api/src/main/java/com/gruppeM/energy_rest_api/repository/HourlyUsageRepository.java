package com.gruppeM.energy_rest_api.repository;

import com.gruppeM.energy_rest_api.model.HourlyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing the 'hourly_usage' table.
 * Provides methods for querying usage data based on time ranges.
 */
public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, Instant> {

    /**
     * Finds all hourly usage records between the given time range (inclusive).
     * Used for historical data queries.
     *
     * @param start start of the time range (inclusive)
     * @param end   end of the time range (inclusive)
     * @return list of HourlyUsage objects
     */
    List<HourlyUsage> findAllByHourKeyBetween(Instant start, Instant end);

    /**
     * Retrieves the earliest available hourKey in the database.
     * Used to calculate the start of the available data range.
     *
     * @return Optional with minimum timestamp or empty if no data exists
     */
    @Query("SELECT MIN(h.hourKey) FROM HourlyUsage h")
    Optional<Instant> findMinHourKey();

    /**
     * Retrieves the most recent (latest) hourKey in the database.
     * Used to calculate the end of the available data range.
     *
     * @return Optional with maximum timestamp or empty if no data exists
     */
    @Query("SELECT MAX(h.hourKey) FROM HourlyUsage h")
    Optional<Instant> findMaxHourKey();

    /**
     * Retrieves the most recent usage record (latest timestamp).
     * Alternative to querying max + then using findById().
     *
     * @return The most recent HourlyUsage entry
     */
    HourlyUsage findTopByOrderByHourKeyDesc();
}
