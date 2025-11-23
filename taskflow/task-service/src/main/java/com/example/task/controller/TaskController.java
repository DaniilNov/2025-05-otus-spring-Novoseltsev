package com.example.task.controller;

import com.example.task.dto.CreateTaskRequest;
import com.example.task.dto.TaskDto;
import com.example.task.dto.UpdateTaskRequest;
import com.example.task.exception.UnauthorizedAccessException;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
@Tag(name = "Задачи", description = "Эндпоинты для управления задачами")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;


    @Operation(summary = "Создать задачу",
            description = "Создание новой задачи. Автоматически назначается создатель и статус NEW")
    @ApiResponse(responseCode = "200", description = "Задача создана")
    @ApiResponse(responseCode = "400", description = "Невалидные данные или workflow не существует")
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = getCurrentUserId();
        return taskService.createTask(request, userId);
    }

    @Operation(summary = "Получить мои задачи",
            description = "Получить задачи где пользователь создатель или исполнитель. ADMIN видит все задачи")
    @ApiResponse(responseCode = "200", description = "Список задач получен")
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasksForCurrentUser(
            @Parameter(description = "Фильтр по исполнителю", example = "123")
            @RequestParam(name = "assigneeId", required = false) Long assigneeId,
            @Parameter(description = "Фильтр по статусу", examples = {
                    @ExampleObject(name = "Новые", value = "NEW"),
                    @ExampleObject(name = "В работе", value = "IN_PROGRESS"),
                    @ExampleObject(name = "Завершенные", value = "DONE")
            })
            @RequestParam(name = "status", required = false) String status) {
        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();

        boolean isAdmin = "ADMIN".equals(userRole);

        if (isAdmin) {
            return taskService.getAllTasksWithFilters(assigneeId, status);
        } else {
            if (assigneeId != null && !assigneeId.equals(userId)) {

                throw new UnauthorizedAccessException("You can only filter by your own assignee ID");
            }
            return taskService.getTasksByCreatorOrAssignee(userId, assigneeId, status);
        }
    }

    @Operation(summary = "Обновить статус",
            description = "Изменение статуса задачи. Проверяются права и валидность перехода через workflow")
    @ApiResponse(responseCode = "200", description = "Статус обновлен")
    @ApiResponse(responseCode = "400", description = "Невалидный переход статуса")
    @ApiResponse(responseCode = "403", description = "Нет прав на изменение")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable("id") Long taskId,
            @Parameter(description = "Новый статус", required = true, example = "IN_PROGRESS")
            @RequestParam("newStatus") String newStatus) {
        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();

        return taskService.updateTaskStatus(taskId, newStatus, userId, userRole);
    }

    @Operation(summary = "Обновить задачу",
            description = "Обновление заголовка, описания или исполнителя задачи. Только создатель может изменять")
    @ApiResponse(responseCode = "200", description = "Задача обновлена",
            content = @Content(schema = @Schema(implementation = TaskDto.class)))
    @ApiResponse(responseCode = "400", description = "Невалидные данные или исполнитель не существует")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    @ApiResponse(responseCode = "403", description = "Нет прав на изменение")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления задачи",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateTaskRequest.class))
            )
            @Valid @RequestBody UpdateTaskRequest request,
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable("id") Long taskId) {

        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();

        return taskService.updateTask(taskId, request, userId, userRole);
    }

    @Operation(summary = "Удалить задачу",
            description = "Удаление задачи. Только создатель может удалять свои задачи")
    @ApiResponse(responseCode = "204", description = "Задача успешно удалена")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    @ApiResponse(responseCode = "403", description = "Нет прав на удаление")
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable("id") Long taskId) {
        Long userId = getCurrentUserId();
        String userRole = getCurrentUserRole();

        return taskService.deleteTask(taskId, userId, userRole);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new RuntimeException("Пользователь не аутентифицирован");
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication object in getCurrentUserRole: {}", auth);
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            GrantedAuthority authority = auth.getAuthorities().iterator().next();

            String authorityString = authority.getAuthority();
            log.debug("Raw authority from authentication: {}", authorityString);

            if (authorityString.startsWith("ROLE_")) {
                String extractedRole = authorityString.substring("ROLE_".length());
                log.debug("Extracted role for controller: {}", extractedRole);
                return extractedRole;
            }
            log.debug("Authority does not start with ROLE_, returning as-is: {}", authorityString);
            return authorityString;
        }
        log.warn("Authentication is null, authorities are null, or authorities are empty. Cannot get user role.");
        throw new RuntimeException("Роль пользователя не найдена в аутентификации");
    }

}