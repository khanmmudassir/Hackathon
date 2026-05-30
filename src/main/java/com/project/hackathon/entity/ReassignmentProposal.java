package com.project.hackathon.entity;

import com.project.hackathon.constants.SuggestionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reassignment_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignmentProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orderId;
    private String originalAgentId;
    private String recommendedAgentId;

    private String reason;
    @Enumerated(EnumType.STRING)
    private SuggestionStatus status;

    private Double confidenceScore;
}
