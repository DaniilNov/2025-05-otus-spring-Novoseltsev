package com.example.task.service;

import com.example.task.dto.CreateTaskRequest;
import com.example.task.dto.TaskDto;
import com.example.task.dto.UpdateTaskRequest;
import com.example.task.entity.Task;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.exception.UnauthorizedAccessException;
import com.example.task.exception.WorkflowNotFoundException;
import com.example.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final WorkflowValidationService workflowValidationService;

    private final RestClient authRestClient;

    @Value("${workflow.id.default}")
    private Long defaultWorkFlowId;

    public TaskService(TaskRepository taskRepository,
                       WorkflowValidationService workflowValidationService,
                       @Qualifier("authRestClient") RestClient authRestClient) {
        this.taskRepository = taskRepository;
        this.workflowValidationService = workflowValidationService;
        this.authRestClient = authRestClient;
    }

    public ResponseEntity<TaskDto> createTask(CreateTaskRequest request, Long userId) {

        Long workflowId = request.getWorkflowId() == null ? defaultWorkFlowId : request.getWorkflowId();

        if (workflowId != null && !workflowValidationService.checkWorkflowExists(workflowId)) {
            throw new WorkflowNotFoundException("Workflow not found with id: " + workflowId);
        }
        if (request.getAssigneeId() != null && !userExists(request.getAssigneeId())) {
            throw new RuntimeException("Assignee user does not exist with id: " + request.getAssigneeId());
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status("NEW")
                .assigneeId(request.getAssigneeId())
                .creatorId(userId)
                .workflowId(workflowId)
                .build();

        task = taskRepository.save(task);

        TaskDto taskDto = mapToDto(task);
        return ResponseEntity.ok(taskDto);
    }

    public ResponseEntity<List<TaskDto>> getTasksByCreatorOrAssignee(Long userId, Long assigneeId, String status) {
        List<Task> tasks;

        if (assigneeId != null) {
            tasks = taskRepository.findByAssigneeId(assigneeId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
            tasks = filterByCreatorOrAssignee(tasks, userId);
        } else {
            tasks = taskRepository.findByCreatorIdOrAssigneeId(userId, userId);
        }

        List<TaskDto> dtos = tasks.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public ResponseEntity<List<TaskDto>> getAllTasksWithFilters(Long assigneeId, String status) {
        List<Task> tasks;

        if (assigneeId != null && status != null) {
            tasks = taskRepository.findByAssigneeIdAndStatus(assigneeId, status);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByAssigneeId(assigneeId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        List<TaskDto> dtos = tasks.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    private List<Task> filterByCreatorOrAssignee(List<Task> tasks, Long userId) {
        return tasks.stream()
                .filter(t -> t.getCreatorId().equals(userId)
                        || (t.getAssigneeId() != null && t.getAssigneeId().equals(userId)))
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> updateTaskStatus(Long id, String newStatus, Long userId, String userRole) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        boolean hasPermission = "ADMIN".equals(userRole) ||
                Objects.equals(task.getCreatorId(), userId) ||
                Objects.equals(task.getAssigneeId(), userId);

        if (!hasPermission) {
            return ResponseEntity.status(403).body(Map.of("error", "You do not have permission to modify this task"));
        }

        if (task.getWorkflowId() != null) {
            log.debug("Validating workflow transition for task {} from {} to {} for role {}", id, task.getStatus(),
                    newStatus, userRole);
            if ("ADMIN".equals(userRole)) {
                log.debug("User is ADMIN, skipping workflow validation for task {}", id);
            } else {
                boolean isValid = workflowValidationService.isValidTransition(task.getWorkflowId(), task.getStatus(),
                        newStatus, userRole);
                if (!isValid) {
                    log.warn("Invalid status transition for task {} from {} to {} for role {}", id, task.getStatus(),
                            newStatus, userRole);
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid status transition for your role"));
                }
            }
        } else {
            log.debug("Task {} has no workflow ID, skipping workflow validation.", id);
        }

        task.setStatus(newStatus);
        task = taskRepository.save(task);

        return ResponseEntity.ok(mapToDto(task));
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public TaskDto mapToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setAssigneeId(task.getAssigneeId());
        dto.setCreatorId(task.getCreatorId());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    @Transactional
    public ResponseEntity<TaskDto> updateTask(Long taskId, UpdateTaskRequest request, Long userId, String userRole) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        boolean hasPermission = "ADMIN".equals(userRole) || Objects.equals(task.getCreatorId(), userId);

        if (!hasPermission) {
            throw new UnauthorizedAccessException("You do not have permission to update this task");
        }

        if (request.getAssigneeId() != null) {
            if (!userExists(request.getAssigneeId())) {
                return ResponseEntity.badRequest().build();
            }
            task.setAssigneeId(request.getAssigneeId());
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        task = taskRepository.save(task);

        TaskDto taskDto = mapToDto(task);
        return ResponseEntity.ok(taskDto);
    }

    @Transactional
    public ResponseEntity<Void> deleteTask(Long taskId, Long userId, String userRole) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        boolean hasPermission = "ADMIN".equals(userRole) || Objects.equals(task.getCreatorId(), userId);

        if (!hasPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        taskRepository.delete(task);
        return ResponseEntity.noContent().build();
    }

    private boolean userExists(Long userId) {
        String path = "/api/users/exists/{id}";

        try {
            Boolean exists = authRestClient.get()
                    .uri(path, userId)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error calling auth service to check user existence: {}", e.getMessage());
            return false;
        }
    }
}