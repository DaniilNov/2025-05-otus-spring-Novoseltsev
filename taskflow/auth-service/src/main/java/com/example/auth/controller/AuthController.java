package com.example.auth.controller;

import com.example.auth.dto.ErrorResponse;
import com.example.auth.dto.JwtResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.entity.User;
import com.example.auth.exception.TokenValidationException;
import com.example.auth.service.AuthService;
import com.example.auth.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации и аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    private final JwtUtil jwtUtil;

    @Operation(summary = "Регистрация", description = "Создание нового пользователя и получение JWT токена")
    @ApiResponse(responseCode = "200", description = "Успешная регистрация",
            content = @Content(schema = @Schema(implementation = JwtResponse.class)))
    @ApiResponse(responseCode = "400", description = "Email уже существует или невалидные данные",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Вход", description = "Аутентификация пользователя и получение JWT токена")
    @ApiResponse(responseCode = "200", description = "Успешный вход",
            content = @Content(schema = @Schema(implementation = JwtResponse.class)))
    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Валидация токена", description = "Проверка действительности JWT токена")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Токен действителен")
    @ApiResponse(responseCode = "401", description = "Токен недействителен или просрочен")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        log.info("Authorization header: {}", tokenHeader);
        String token = extractJwtFromHeader(tokenHeader);
        if (token == null) {
            throw new TokenValidationException("Заголовок Authorization не предоставлен или токен не найден");
        }
        try {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getUsernameFromToken(token);
                User user = authService.getUserByEmail(email);
                if (user != null) {
                    log.info("Token validated for user: {}, role: {}", user.getId(), user.getRole());
                    return ResponseEntity.ok(Map.of("userId", user.getId(), "role", user.getRole()));
                } else {
                    throw new TokenValidationException("Пользователь, связанный с токеном, не найден в базе данных");
                }
            } else {
                throw new TokenValidationException("Недействительный или просроченный токен");
            }
        } catch (Exception e) {
            log.warn("Token validation failed due to exception: {}", e.getMessage());
            throw new TokenValidationException("Проверка токена не удалась", e);
        }
    }

    private String extractJwtFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}