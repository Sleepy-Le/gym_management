package com.gym.repository;

import com.gym.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByOrderByProductIdAsc();
    Page<Product> findAllByOrderByProductIdAsc(Pageable pageable);

    List<Product> findByStockLessThanEqual(Integer threshold);

    List<Product> findByStockGreaterThan(Integer threshold);
}
