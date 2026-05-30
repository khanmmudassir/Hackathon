package com.project.hackathon.data;

import com.project.hackathon.entity.Agent;
import lombok.Builder;

@Builder
public record ReassignmentRecommendation (
    Agent agent,
    Double confidenceScore,
    String explanation
) {}
