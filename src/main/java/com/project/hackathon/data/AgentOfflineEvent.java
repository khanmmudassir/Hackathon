package com.project.hackathon.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class AgentOfflineEvent {
    private final String agentId;
    private final String reason;
}
