package com.gym.repository;

import com.gym.entity.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, Integer> {

    List<SaleOrder> findAllByOrderByOrderDateDesc();
    Page<SaleOrder> findAllByOrderByOrderDateDesc(Pageable pageable);
}
