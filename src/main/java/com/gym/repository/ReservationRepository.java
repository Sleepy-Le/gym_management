package com.gym.repository;

import com.gym.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    /** 教练时间冲突检测 */
    long countByCoachIdAndReserveTimeAndStatusNot(String coachId, LocalDateTime reserveTime, String status);

    List<Reservation> findByMemberIdAndReserveTimeGreaterThanEqualOrderByReserveTimeAsc(String memberId, LocalDateTime time);

    List<Reservation> findByCoachIdAndReserveTimeGreaterThanEqualOrderByReserveTimeAsc(String coachId, LocalDateTime time);

    List<Reservation> findByCoachIdOrderByReserveTimeDesc(String coachId);

    List<Reservation> findByMemberIdOrderByReserveTimeDesc(String memberId);
}
