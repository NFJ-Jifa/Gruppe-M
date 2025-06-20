package com.gruppem.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

/**
 * JPA repository for HourlyUsage entity.
 */
public interface HourlyUsageRepository extends JpaRepository<HourlyUsage, Instant> {
}
