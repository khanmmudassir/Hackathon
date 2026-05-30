package com.project.hackathon.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.project.hackathon.constants.AgentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    @Id
    private String id;

    private String name;

    @Column(name = "active_order_count")
    private Integer activeOrderCount;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    @OneToMany(mappedBy = "assignedAgent")
    @JsonManagedReference
    private List<Order> orders;
}
