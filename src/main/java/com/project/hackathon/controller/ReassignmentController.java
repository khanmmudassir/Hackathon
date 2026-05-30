package com.project.hackathon.controller;

import com.project.hackathon.constants.AgentStatus;
import com.project.hackathon.constants.SuggestionStatus;
import com.project.hackathon.entity.Order;
import com.project.hackathon.entity.ReassignmentProposal;
import com.project.hackathon.repository.OrderRepository;
import com.project.hackathon.repository.ProposalRepository;
import com.project.hackathon.service.ReassignmentEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReassignmentController {

    private final ProposalRepository proposalRepository;
    private final OrderRepository orderRepository;
    private final ReassignmentEngine reassignmentEngine;


    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }


    @GetMapping("/orders")
    public List<Order> getOrders(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByStatus(status);
        }
        return orderRepository.findAll();
    }

    @PatchMapping("/agents/{id}/status")
    public ResponseEntity<Void> updateAgentStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        AgentStatus newStatus = AgentStatus.valueOf(body.get("status"));
        reassignmentEngine.updateAgentAvailability(id,newStatus);

        if ("OFFLINE".equalsIgnoreCase(String.valueOf(newStatus))) {
            reassignmentEngine.handleAgentFailure(id, "Agent manually marked as offline");
        }

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/suggestions/{id}")
    public ResponseEntity<Void> updateSuggestion(@PathVariable String id, @RequestBody Map<String, String> body) {
        SuggestionStatus status = SuggestionStatus.valueOf(body.get("status"));

        switch (status) {
            case ACCEPTED -> reassignmentEngine.approveReassignment(id);
            case REJECTED -> reassignmentEngine.rejectReassignment(id);
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok().build();
    }

    // Existing endpoint for dashboard polling
    @GetMapping("/reassignments/proposals")
    public List<ReassignmentProposal> getPendingProposals() {
        return proposalRepository.findByStatus(SuggestionStatus.PENDING);
    }
}
