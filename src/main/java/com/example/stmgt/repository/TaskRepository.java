package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCreatedByIdOrderByDueDateDesc(Long createdById);

    List<Task> findByCreatedByIdOrderByDueDateAsc(Long createdById);

    List<Task> findByAssignedStudentsId(Long studentUserId);

    long countByCreatedById(Long createdById);
}
