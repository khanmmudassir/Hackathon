package com.project.hackathon.service;

import com.project.hackathon.configuration.LLMGateway;
import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAdvisor {
    private final LLMGateway llmGateway; // Use your manual gateway

    public ReassignmentRecommendation analyzeAndRecommend(Order order, List<Agent> agents) {
        if (agents.isEmpty()) return null;
        String agentContext = agents.stream()
                .map(a -> String.format("[ID: %s, Load: %d]", a.getId(), a.getActiveOrderCount()))
                .collect(Collectors.joining(", "));
        String prompt = String.format(
                "Order %s needs reassignment. Available agents: %s. " +
                        "Pick the best agent and provide a 1-sentence reasoning.",
                order.getId(), agentContext
        );

        String response = llmGateway.callLLM(prompt);
        Agent selected = agents.stream()
                .filter(a -> response.contains(a.getId()))
                .findFirst()
                .orElse(agents.get(0)); // Fallback to first if AI hallucinates

        // Simple logic to parse the agent ID from LLM text or just pick the first for now
        return new ReassignmentRecommendation(agents.get(0), 0.95, response);
    }
}
