package com.project.hackathon.service;

import com.project.hackathon.constants.AgentStatus;
import com.project.hackathon.constants.OrderStatus;
import com.project.hackathon.constants.SuggestionStatus;
import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import com.project.hackathon.entity.ReassignmentProposal;
import com.project.hackathon.repository.AgentRepository;
import com.project.hackathon.repository.OrderRepository;
import com.project.hackathon.repository.ProposalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReassignmentEngine {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final ProposalRepository proposalRepository;
    private final RoutingEngine routingEngine;

    /**
     * Triggered when an agent goes offline mid-shift.
     */
    @Transactional
    public void handleAgentFailure(String agentId, String reason) {
        List<Order> affectedOrders = orderRepository.findByAssignedAgentIdAndStatus(agentId, OrderStatus.ASSIGNED);
        if (affectedOrders.isEmpty()) return;

        for (Order order : affectedOrders) {
            proposeNewAgent(order, List.of(agentId), reason);
        }
    }

    @Transactional
    public void approveReassignment(String proposalId) {
        ReassignmentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Order order = orderRepository.findById(proposal.getOrderId()).get();
        Agent newAgent = agentRepository.findById(proposal.getRecommendedAgentId()).get();
        Agent oldAgent = agentRepository.findById(proposal.getOriginalAgentId()).get();
        order.setAssignedAgent(newAgent);
        order.setStatus(OrderStatus.REASSIGNED);

        newAgent.setActiveOrderCount(newAgent.getActiveOrderCount() + 1);
        oldAgent.setActiveOrderCount(Math.max(0, oldAgent.getActiveOrderCount() - 1));

        proposal.setStatus(SuggestionStatus.ACCEPTED);

        orderRepository.save(order);
        agentRepository.save(newAgent);
        agentRepository.save(oldAgent);
        proposalRepository.save(proposal);
    }

    @Transactional
    public void updateAgentAvailability(String agentId, AgentStatus newStatus) {

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + agentId));
        agent.setStatus(newStatus);
        agentRepository.save(agent);
    }

    @Transactional
    public void rejectReassignment(String proposalId) {
        ReassignmentProposal oldProposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Reassignment proposal not found with ID: " + proposalId));

        oldProposal.setStatus(SuggestionStatus.REJECTED);
        proposalRepository.save(oldProposal);

        Order order = orderRepository.findById(oldProposal.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        List<String> excludedAgents = List.of(oldProposal.getOriginalAgentId(), oldProposal.getRecommendedAgentId());
        proposeNewAgent(order, excludedAgents, "Previous suggestion was rejected by operator.");
    }

    private void proposeNewAgent(Order order, List<String> excludedAgentIds, String reason) {
        List<Agent> eligibleAgents = agentRepository.findByStatusAndActiveOrderCountLessThanOrderByActiveOrderCountAsc(
                AgentStatus.AVAILABLE, 5);

        List<Agent> candidates = eligibleAgents.stream()
                .filter(agent -> !excludedAgentIds.contains(agent.getId()))
                .collect(Collectors.toList());
        try {
            ReassignmentRecommendation result = routingEngine.getBestAgent(order, candidates);

            if (result != null && result.agent() != null) {
                ReassignmentProposal newProposal = ReassignmentProposal.builder()
                        .orderId(order.getId())
                        .originalAgentId(excludedAgentIds.get(0))
                        .recommendedAgentId(result.agent().getId())
                        .reason(reason)
                        .status(SuggestionStatus.PENDING)
                        .confidenceScore(result.confidenceScore())
                        .build();

                proposalRepository.save(newProposal);
            }
        } catch(Exception e) {
            routingEngine.setStrategy("ruleBased");
            ReassignmentRecommendation result = routingEngine.getBestAgent(order, candidates);

            if (result != null && result.agent() != null) {
                ReassignmentProposal newProposal = ReassignmentProposal.builder()
                        .orderId(order.getId())
                        .originalAgentId(excludedAgentIds.get(0))
                        .recommendedAgentId(result.agent().getId())
                        .reason(reason)
                        .status(SuggestionStatus.PENDING)
                        .confidenceScore(result.confidenceScore())
                        .build();

                proposalRepository.save(newProposal);
            }
        }
    }
}
