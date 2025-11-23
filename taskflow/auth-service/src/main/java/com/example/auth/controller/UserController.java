package com.example.auth.controller;

import com.example.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "Пользователи", description = "Эндпоинты для работы с пользователями")
public class UserController {

    private final UserRepository userRepository;

    @Operation(summary = "Проверить существование пользователя",
            description = "Проверяет, существует ли пользователь с указанным ID.")
    @ApiResponse(responseCode = "200", description = "Результат проверки",
            content = @Content(schema = @Schema(implementation = Boolean.class)))
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> userExists(
            @Parameter(description = "ID пользователя", required = true) @PathVariable("id") Long id) {
        boolean exists = userRepository.existsById(id);
        return ResponseEntity.ok(exists);
    }
}