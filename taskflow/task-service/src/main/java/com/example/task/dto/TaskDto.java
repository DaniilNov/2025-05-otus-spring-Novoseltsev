package com.example.task.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long id;

    private String title;

    private String description;

    private String status;

    private Long assigneeId;

    private Long creatorId;

    private LocalDateTime createdAt;
}