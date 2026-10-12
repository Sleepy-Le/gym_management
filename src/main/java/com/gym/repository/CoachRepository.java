package com.gym.repository;

import com.gym.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoachRepository extends JpaRepository<Coach, String> {

    List<Coach> findAllByOrderByCoachIdAsc();
    Page<Coach> findAllByOrderByCoachIdAsc(Pageable pageable);

    List<Coach> findByStatus(String status);
}
