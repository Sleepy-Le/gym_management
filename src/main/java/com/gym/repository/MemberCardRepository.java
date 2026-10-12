package com.gym.repository;

import com.gym.entity.MemberCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberCardRepository extends JpaRepository<MemberCard, Integer> {

    List<MemberCard> findByMemberIdOrderByCardIdDesc(String memberId);

    List<MemberCard> findByMemberIdAndStatus(String memberId, String status);

    long countByMemberIdAndStatus(String memberId, String status);
}
