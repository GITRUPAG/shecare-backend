package com.app.shecare.repository;

import com.app.shecare.entity.PeriodLog;
import com.app.shecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodLogRepository extends JpaRepository<PeriodLog, Long> {

    List<PeriodLog> findByUserOrderByStartDateDesc(User user);

    List<PeriodLog> findTop6ByUserOrderByStartDateDesc(User user);

}