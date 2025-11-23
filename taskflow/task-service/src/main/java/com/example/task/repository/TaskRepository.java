package com.example.task.repository;

import com.example.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCreatorId(Long creatorId);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByStatus(String status);

    List<Task> findByCreatorIdOrAssigneeId(Long userId, Long userId1);

    List<Task> findByAssigneeIdAndStatus(Long assigneeId, String status);
}