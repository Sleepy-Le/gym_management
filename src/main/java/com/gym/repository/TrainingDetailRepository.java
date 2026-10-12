package com.gym.repository;

import com.gym.entity.TrainingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingDetailRepository extends JpaRepository<TrainingDetail, Integer> {

    List<TrainingDetail> findByLogIdOrderByDetailIdAsc(Integer logId);
}
