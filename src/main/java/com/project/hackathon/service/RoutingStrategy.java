package com.project.hackathon.service;

import com.project.hackathon.data.ReassignmentRecommendation;
import com.project.hackathon.entity.Agent;
import com.project.hackathon.entity.Order;

import java.util.List;

public interface RoutingStrategy {
    ReassignmentRecommendation recommend(Order order, List<Agent> availableAgents);
}
