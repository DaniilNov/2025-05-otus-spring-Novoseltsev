package com.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;

    private String description;

    private Long assigneeId;

    private Long workflowId;
}