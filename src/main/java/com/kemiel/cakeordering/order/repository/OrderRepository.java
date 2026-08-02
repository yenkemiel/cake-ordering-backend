package com.kemiel.cakeordering.order.repository;

import com.kemiel.cakeordering.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 訂單 Repository
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNoAndPhone(String orderNo, String phone);

    Page<Order> findByStatus(String status, Pageable pageable);
}