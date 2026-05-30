package com.project.hackathon.service;

import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("aiPowered")
@RequiredArgsConstructor
public class AiRoutingStrategy implements RoutingStrategy {
    private final AiAdvisor aiAdvisor;

    @Override
    public ReassignmentRecommendation recommend(Order order, List<Agent> agents) {
        return aiAdvisor.analyzeAndRecommend(order, agents);
    }
}
