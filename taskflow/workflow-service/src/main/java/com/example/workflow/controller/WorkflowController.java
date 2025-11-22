package com.example.workflow.controller;

import com.example.workflow.entity.Workflow;
import com.example.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
@Tag(name = "Воркфлоу", description = "Эндпоинты для управления воркфлоу и переходами")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Operation(summary = "Создать workflow",
            description = "Создание нового workflow со статусами и правилами переходов")
    @ApiResponse(responseCode = "200", description = "Workflow создан")
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody Workflow workflow) {
        Workflow saved = workflowService.createWorkflow(workflow);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Получить workflow", description = "Получение workflow по ID")
    @ApiResponse(responseCode = "200", description = "Workflow найден")
    @ApiResponse(responseCode = "404", description = "Workflow не найден")
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(
            @Parameter(description = "ID workflow", required = true, example = "1")
            @PathVariable("id") Long id) {
        Workflow workflow = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(workflow);
    }

    @Operation(summary = "Проверить переход",
            description = "Проверка разрешен ли переход между статусами для указанной роли")
    @ApiResponse(responseCode = "200", description = "Переход разрешен/запрещен")
    @ApiResponse(responseCode = "404", description = "Workflow не найден")
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateTransition(
            @Parameter(description = "ID workflow", required = true, example = "1")
            @PathVariable("id") Long id,
            @Parameter(description = "Исходный статус", required = true, example = "NEW")
            @RequestParam("fromStatus") String fromStatus,
            @Parameter(description = "Целевой статус", required = true, example = "IN_PROGRESS")
            @RequestParam("toStatus") String toStatus,
            @Parameter(description = "Роль пользователя", required = true, example = "USER")
            @RequestParam("role") String role) {
        boolean valid = workflowService.isValidTransition(id, fromStatus, toStatus, role);
        return ResponseEntity.ok(valid);
    }
}