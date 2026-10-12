package com.gym.repository;

import com.gym.entity.BodyMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BodyMeasureRepository extends JpaRepository<BodyMeasure, Integer> {

    List<BodyMeasure> findByMemberIdOrderByMeasureDateDesc(String memberId);
}
