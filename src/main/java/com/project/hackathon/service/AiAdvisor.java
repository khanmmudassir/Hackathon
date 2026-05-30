package com.project.hackathon.service;

import com.project.hackathon.configuration.LLMGateway;
import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiAdvisor {
    private final LLMGateway llmGateway; // Use your manual gateway

    public ReassignmentRecommendation analyzeAndRecommend(Order order, List<Agent> agents) {
        String prompt = String.format(
                "Order %s needs reassignment. Available agents: %s. " +
                        "Pick the best agent and provide a 1-sentence reasoning.",
                order.getId(), agents.toString()
        );

        String response = llmGateway.callLLM(prompt);

        // Simple logic to parse the agent ID from LLM text or just pick the first for now
        return new ReassignmentRecommendation(agents.get(0), 0.95, response);
    }
}
