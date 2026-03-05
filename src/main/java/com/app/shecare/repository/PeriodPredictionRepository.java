package com.app.shecare.repository;

import com.app.shecare.entity.PeriodPrediction;
import com.app.shecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeriodPredictionRepository extends JpaRepository<PeriodPrediction, Long> {

    Optional<PeriodPrediction> findTopByUserOrderByPredictedStartDateDesc(User user);

    

}