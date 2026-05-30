package com.project.hackathon.controller;

import com.project.hackathon.constants.ProposalStatus;
import com.project.hackathon.entity.ReassignmentProposal;
import com.project.hackathon.repository.ProposalRepository;
import com.project.hackathon.service.ReassignmentEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reassignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // For React development
public class ReassignmentController {

    private final ProposalRepository proposalRepository;
    private final ReassignmentEngine reassignmentEngine;

    // 1. Get all pending recommendations for the Ops Dashboard
    @GetMapping("/proposals")
    public List<ReassignmentProposal> getPendingProposals() {
        return proposalRepository.findAll().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .toList();
    }

    // 2. Approve a recommendation (commits changes to Order/Agent)
    @PostMapping("/approve/{id}")
    public ResponseEntity<Void> approve(@PathVariable String id) {
        reassignmentEngine.approveReassignment(id);
        return ResponseEntity.ok().build();
    }

    // 3. Reject a recommendation
    @PostMapping("/reject/{id}")
    public ResponseEntity<Void> reject(@PathVariable String id) {
        ReassignmentProposal proposal = proposalRepository.findById(id)
                .orElseThrow();
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
        return ResponseEntity.ok().build();
    }

    // 4. Manual trigger for testing: Simulate an agent going offline
    @PostMapping("/simulate-failure")
    public ResponseEntity<Void> simulateFailure(@RequestParam String agentId, @RequestParam String reason) {
        reassignmentEngine.handleAgentFailure(agentId, reason);
        return ResponseEntity.ok().build();
    }
}
