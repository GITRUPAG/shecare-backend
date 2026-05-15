package com.app.shecare.repository;

import com.app.shecare.entity.HealthMetric;
import com.app.shecare.entity.HealthMetric.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {

    Page<HealthMetric> findByUserIdAndTypeAndRecordedAtBetween(
            Long userId, MetricType type, Instant from, Instant to, Pageable pageable);

    List<HealthMetric> findByUserIdAndTypeAndRecordedAtBetween(
            Long userId, MetricType type, Instant from, Instant to);

    List<HealthMetric> findByUserIdOrderByRecordedAtDesc(Long userId);
}