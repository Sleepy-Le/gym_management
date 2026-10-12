package com.gym.repository;

import com.gym.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> findAllByOrderByCourseIdAsc();
    Page<Course> findAllByOrderByCourseIdAsc(Pageable pageable);
}
