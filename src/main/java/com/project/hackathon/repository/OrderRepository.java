package com.project.hackathon.repository;

import com.project.hackathon.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByAssignedAgentIdAndStatus(String agentId, String status);
    List<Order> findByAssignedAgentId(String agentId);
}
