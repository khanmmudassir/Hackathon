package com.project.hackathon.repository;

import com.project.hackathon.constants.SuggestionStatus;
import com.project.hackathon.entity.ReassignmentProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<ReassignmentProposal, String> {
    List<ReassignmentProposal> findByStatus(SuggestionStatus suggestionStatus);
}
