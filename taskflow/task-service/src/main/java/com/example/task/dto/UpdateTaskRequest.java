package com.example.task.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateTaskRequest {
    @Size(max = 255, message = "Title must be at most 255 characters long")
    private String title;

    private String description;

    private Long assigneeId;
}