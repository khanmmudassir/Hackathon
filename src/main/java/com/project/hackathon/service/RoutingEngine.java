package com.project.hackathon.service;

import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoutingEngine {

    private final Map<String, RoutingStrategy> strategies;
    private volatile String currentStrategyName = "ruleBasedRouting"; // Default strategy

    public RoutingEngine(Map<String, RoutingStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Switches the active routing strategy at runtime.
     * @param strategyName The bean name of the desired strategy (e.g., "aiRoutingStrategy")
     */
    public void setStrategy(String strategyName) {
        if (!strategies.containsKey(strategyName)) {
            throw new IllegalArgumentException("Strategy not found: " + strategyName);
        }
        this.currentStrategyName = strategyName;
    }

    /**
     * Delegates the recommendation logic to the currently active strategy.
     */
    public ReassignmentRecommendation getBestAgent(Order order, List<Agent> candidates) {
        RoutingStrategy strategy = strategies.get(currentStrategyName);

        if (candidates == null || candidates.isEmpty()) {
            return ReassignmentRecommendation.builder()
                    .explanation("No active agents available for reassignment.")
                    .confidenceScore(0.0)
                    .build();
        }

        return strategy.recommend(order, candidates);
    }

    public String getCurrentStrategyName() {
        return currentStrategyName;
    }
}
