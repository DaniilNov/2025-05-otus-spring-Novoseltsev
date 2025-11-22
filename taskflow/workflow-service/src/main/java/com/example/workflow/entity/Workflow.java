package com.example.workflow.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "workflows")
@Data
@NoArgsConstructor
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "workflow_statuses", joinColumns = @JoinColumn(name = "workflow_id"))
    @Column(name = "status_name")
    private List<String> statuses;

    @ElementCollection
    @CollectionTable(name = "workflow_transitions", joinColumns = @JoinColumn(name = "workflow_id"))
    @Embedded
    private List<WorkflowTransition> transitions;

    public Workflow(String name, List<String> statuses, List<WorkflowTransition> transitions) {
        this.name = name;
        this.statuses = statuses;
        this.transitions = transitions;
    }
}