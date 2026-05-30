package com.project.hackathon.service;

import com.project.hackathon.constants.AgentStatus;
import com.project.hackathon.constants.ProposalStatus;
import com.project.hackathon.data.AgentOfflineEvent;
import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import com.project.hackathon.entity.ReassignmentProposal;
import com.project.hackathon.repository.AgentRepository;
import com.project.hackathon.repository.OrderRepository;
import com.project.hackathon.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReassignmentLoop {
    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final RoutingEngine routingEngine;
    private final ProposalRepository proposalRepository;

    @EventListener
    @Transactional
    public void onAgentStatusChange(AgentOfflineEvent event) {
        List<Order> affectedOrders = orderRepository.findByAssignedAgentId(event.getAgentId())
                .stream().filter(Order::isReassignable).toList();

        List<Agent> candidates = agentRepository.findByStatus(AgentStatus.AVAILABLE);

        for (Order order : affectedOrders) {
            ReassignmentRecommendation rec = routingEngine.getBestAgent(order, candidates);

            ReassignmentProposal proposal = ReassignmentProposal.builder()
                    .orderId(order.getId())
                    .recommendedAgentId(rec.agent().getId())
                    .reason(rec.explanation()) // The AI's plain English explanation
                    .confidenceScore(rec.confidenceScore())
                    .status(ProposalStatus.PENDING)
                    .build();

            proposalRepository.save(proposal);
        }
    }
}
