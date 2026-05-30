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
        // 1. Retrieve the proposal
        ReassignmentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found: " + proposalId));

        // 2. Retrieve involved entities
        Order order = orderRepository.findById(proposal.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Agent newAgent = agentRepository.findById(proposal.getRecommendedAgentId())
                .orElseThrow(() -> new EntityNotFoundException("Target agent not found"));

        Agent oldAgent = agentRepository.findById(proposal.getOriginalAgentId())
                .orElseThrow(() -> new EntityNotFoundException("Original agent not found"));

        // 3. Update Order state
        order.setAssignedAgent(newAgent);
        order.setStatus(OrderStatus.REASSIGNED);

        // 4. Update Old Agent state (Decrement load)
        int currentOldLoad = oldAgent.getActiveOrderCount();
        oldAgent.setActiveOrderCount(Math.max(0, currentOldLoad - 1));

        // If the old agent was BUSY but now has capacity, set back to AVAILABLE
        if (oldAgent.getStatus() == AgentStatus.BUSY) {
            oldAgent.setStatus(AgentStatus.AVAILABLE);
        }
        if (newAgent.getStatus() != AgentStatus.AVAILABLE) {
            // Mark this proposal as rejected because the agent is no longer eligible
            proposal.setStatus(SuggestionStatus.REJECTED);
            proposalRepository.save(proposal);

            // Trigger a new search for a different agent immediately
            List<String> excludedAgents = List.of(proposal.getOriginalAgentId(), newAgent.getId());
            proposeNewAgent(order, excludedAgents, "Selected agent went offline before approval.");

            throw new IllegalStateException("Cannot approve: Agent " + newAgent.getId() + " is no longer AVAILABLE.");
        }

        // 5. Update New Agent state (Increment load & Update Status)
        newAgent.setActiveOrderCount(newAgent.getActiveOrderCount() + 1);

        // LOGIC: Mark as BUSY if they have reached a certain threshold (e.g., 1 or more orders)
        // Or if your business logic dictates they are BUSY as soon as they have an active order
        newAgent.setStatus(AgentStatus.BUSY);

        // 6. Finalize Proposal
        proposal.setStatus(SuggestionStatus.ACCEPTED);

        // 7. Persist changes
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
                createProposal(order,excludedAgentIds.get(0),result.agent(),result.confidenceScore(),reason);
            }
        } catch(Exception e) {
            routingEngine.setStrategy("ruleBased");
            ReassignmentRecommendation result = routingEngine.getBestAgent(order, candidates);
            if (result != null && result.agent() != null) {
            createProposal(order,excludedAgentIds.get(0),result.agent(),result.confidenceScore(),reason);
            }
        }
    }

    private void createProposal(Order order, String originalAgentId, Agent recommendedAgent, Double confidence, String reason) {
        ReassignmentProposal newProposal = ReassignmentProposal.builder()
                .orderId(order.getId())
                .originalAgentId(originalAgentId)
                .recommendedAgentId(recommendedAgent.getId())
                .reason(reason)
                .status(SuggestionStatus.PENDING)
                .confidenceScore(confidence)
                .build();
        proposalRepository.save(newProposal);
    }
}
