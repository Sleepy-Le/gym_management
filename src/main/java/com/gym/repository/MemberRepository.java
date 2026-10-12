package com.gym.repository;

import com.gym.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String> {

    /** 关键词搜索：编号/姓名/电话 */
    @Query(value = "select m from Member m where :kw is null or :kw = '' or m.memberId like %:kw% or m.name like %:kw% or m.phone like %:kw% order by m.registerDate desc",
            countQuery = "select count(m) from Member m where :kw is null or :kw = '' or m.memberId like %:kw% or m.name like %:kw% or m.phone like %:kw%")
    Page<Member> search(@Param("kw") String kw, Pageable pageable);

    List<Member> findByStatus(String status);
}
