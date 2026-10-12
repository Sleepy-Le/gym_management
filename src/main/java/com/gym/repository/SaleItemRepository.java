package com.gym.repository;

import com.gym.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {

    List<SaleItem> findByOrderIdOrderByItemIdAsc(Integer orderId);
}
