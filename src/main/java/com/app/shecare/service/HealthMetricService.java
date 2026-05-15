package com.app.shecare.service;

import com.app.shecare.dto.HealthMetricRequest;
import com.app.shecare.dto.HealthMetricResponse;
import com.app.shecare.entity.HealthMetric;
import com.app.shecare.entity.HealthMetric.MetricType;
import com.app.shecare.repository.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository repo;
    private final NotificationService notificationService;

    @Transactional
    public void saveBatch(Long userId, List<HealthMetricRequest> requests) {
        List<HealthMetric> entities = requests.stream()
                .map(req -> HealthMetric.builder()
                        .userId(userId)
                        .type(req.type())
                        .value(req.value())
                        .valueSecondary(req.valueSecondary())
                        .recordedAt(req.recordedAt())
                        .source(req.source())
                        .build())
                .toList();

        List<HealthMetric> saved = repo.saveAll(entities);

        // Check each saved metric — fire FCM alert if abnormal
        saved.forEach(metric -> notificationService.checkAndNotify(userId, metric));
    }

    public Page<HealthMetricResponse> query(
            Long userId, MetricType type, Instant from, Instant to, Pageable pageable) {

        return repo.findByUserIdAndTypeAndRecordedAtBetween(userId, type, from, to, pageable)
                   .map(HealthMetricResponse::from);
    }

    public List<HealthMetricResponse> getAll(Long userId) {
        return repo.findByUserIdOrderByRecordedAtDesc(userId)
                   .stream()
                   .map(HealthMetricResponse::from)
                   .toList();
    }
}