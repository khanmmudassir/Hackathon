package com.project.hackathon.service;

import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component("ruleBased")
public class RuleBasedRoutingStrategy implements RoutingStrategy {

    @Override
    public ReassignmentRecommendation recommend(Order order, List<Agent> agents) {
        Agent best = agents.stream()
                .min(Comparator.comparingInt(Agent::getActiveOrderCount))
                .orElseThrow();
        return new ReassignmentRecommendation(best, 0.7, "Lowest current load.");
    }
}
