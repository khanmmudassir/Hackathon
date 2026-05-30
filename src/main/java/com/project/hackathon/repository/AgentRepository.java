package com.project.hackathon.repository;

import com.project.hackathon.constants.AgentStatus;
import com.project.hackathon.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, String> {
    List<Agent> findByStatusAndActiveOrderCountLessThanOrderByActiveOrderCountAsc(String status, Integer maxCapacity);
    List<Agent> findByStatus(AgentStatus status);
}
