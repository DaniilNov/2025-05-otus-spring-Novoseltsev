package com.example.workflow.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class WorkflowTransition {

    private String fromStatus;

    private String toStatus;

    private String allowedRole;

    public WorkflowTransition(String fromStatus, String toStatus, String allowedRole) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.allowedRole = allowedRole;
    }
}