package com.example.posapp.repository;

import com.example.posapp.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
}