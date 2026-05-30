package com.project.hackathon.entity;

import com.project.hackathon.constants.SuggestionStatus;
import com.project.hackathon.constants.TriggerReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reassignment_suggestions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignmentSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId; // Links to the order being reassigned

    @Column(nullable = false)
    private String recommendedAgentId; // The agent the AI thinks is best

    @Column(nullable = false)
    private Double confidenceScore; // Range: 0.0 - 1.0

    @Column(columnDefinition = "TEXT")
    private String reasoning; // The AI's plain-English explanation

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerReason triggerReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
