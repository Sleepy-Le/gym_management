package com.gym.repository;

import com.gym.entity.TrainingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainingLogRepository extends JpaRepository<TrainingLog, Integer> {

    List<TrainingLog> findByMemberIdOrderByTrainingDateDesc(String memberId);

    List<TrainingLog> findByCoachIdOrderByTrainingDateDesc(String coachId);

    List<TrainingLog> findAllByOrderByTrainingDateDesc();
    Page<TrainingLog> findAllByOrderByTrainingDateDesc(Pageable pageable);
    Page<TrainingLog> findByCoachIdOrderByTrainingDateDesc(String coachId, Pageable pageable);
    Page<TrainingLog> findByMemberIdOrderByTrainingDateDesc(String memberId, Pageable pageable);
}
