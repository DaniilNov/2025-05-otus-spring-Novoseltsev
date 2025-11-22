package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;

    private int status;

    private String message;

    private LocalDateTime timestamp;
}