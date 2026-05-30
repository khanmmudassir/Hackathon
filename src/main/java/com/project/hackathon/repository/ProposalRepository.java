package com.project.hackathon.repository;

import com.project.hackathon.entity.ReassignmentProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalRepository extends JpaRepository<ReassignmentProposal, String> {
}
