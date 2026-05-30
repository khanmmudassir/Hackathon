package com.project.hackathon.service;

import com.project.hackathon.constants.ProposalStatus;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import com.project.hackathon.entity.ReassignmentProposal;
import com.project.hackathon.repository.AgentRepository;
import com.project.hackathon.repository.OrderRepository;
import com.project.hackathon.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReassignmentEngine {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final ProposalRepository proposalRepository;

    /**
     * Triggered when an agent goes offline mid-shift.
     */
    @Transactional
    public void handleAgentFailure(String agentId, String reason) {
        // 1. Identify affected orders (those not yet completed)
        List<Order> affectedOrders = orderRepository.findByAssignedAgentIdAndStatus(agentId, "PENDING");

        if (affectedOrders.isEmpty()) return;

        // 2. Find eligible agents (Active status, sorted by lowest current load)
        List<Agent> eligibleAgents = agentRepository.findByStatusAndActiveOrderCountLessThanOrderByActiveOrderCountAsc("ACTIVE", 5);

        // 3. Generate recommendations
        for (int i = 0; i < affectedOrders.size(); i++) {
            Order order = affectedOrders.get(i);

            // Simple Load-Balancing Logic: Distribute orders among eligible agents
            // In Sprint 2/3, this is where the AI/Geospatial logic would plug in
            Agent recommendedAgent = eligibleAgents.get(i % eligibleAgents.size());

            ReassignmentProposal proposal = ReassignmentProposal.builder()
                    .orderId(order.getId())
                    .originalAgentId(agentId)
                    .recommendedAgentId(recommendedAgent.getId())
                    .reason(reason)
                    .status(ProposalStatus.PENDING)
                    .confidenceScore(0.95) // Mock AI score
                    .build();

            proposalRepository.save(proposal);
        }
    }

    /**
     * Called when Ops clicks "Approve" on the dashboard.
     */
    @Transactional
    public void approveReassignment(String proposalId) {
        ReassignmentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Order order = orderRepository.findById(proposal.getOrderId()).get();
        Agent newAgent = agentRepository.findById(proposal.getRecommendedAgentId()).get();
        Agent oldAgent = agentRepository.findById(proposal.getOriginalAgentId()).get();

        // Commit the change
        order.setAssignedAgent(newAgent);

        // Update metadata for load balancing
        newAgent.setActiveOrderCount(newAgent.getActiveOrderCount() + 1);
        oldAgent.setActiveOrderCount(Math.max(0, oldAgent.getActiveOrderCount() - 1));

        proposal.setStatus(ProposalStatus.APPROVED);

        orderRepository.save(order);
        agentRepository.save(newAgent);
        agentRepository.save(oldAgent);
        proposalRepository.save(proposal);
    }
}
