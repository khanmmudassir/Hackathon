package com.project.hackathon.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.hackathon.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    private String description;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_agent_id")
    @JsonIgnoreProperties("orders")
    private Agent assignedAgent;

    public boolean isReassignable() {
        return status == OrderStatus.REASSIGNMENT_PENDING || status == OrderStatus.ASSIGNED;
    }
}
