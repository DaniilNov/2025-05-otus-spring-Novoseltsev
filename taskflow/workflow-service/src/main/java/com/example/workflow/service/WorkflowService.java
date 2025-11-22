package com.example.workflow.service;

import com.example.workflow.entity.Workflow;
import com.example.workflow.exception.WorkflowNotFoundException;
import com.example.workflow.repository.WorkflowRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public Workflow createWorkflow(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    public Workflow getWorkflowById(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with id: " + id));
    }

    public boolean isValidTransition(Long id, String fromStatus, String toStatus, String role) {
        Workflow workflow = getWorkflowById(id);

        return workflow.getTransitions().stream()
                .anyMatch(t -> t.getFromStatus().equals(fromStatus) &&
                        t.getToStatus().equals(toStatus) &&
                        t.getAllowedRole().equals(role));
    }
}