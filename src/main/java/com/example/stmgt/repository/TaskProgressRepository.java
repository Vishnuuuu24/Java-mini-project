package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.TaskProgress;
import com.example.stmgt.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {

    Optional<TaskProgress> findByTaskIdAndStudentId(Long taskId, Long studentId);

    List<TaskProgress> findByTaskId(Long taskId);

    List<TaskProgress> findByStudentId(Long studentId);

    long countByTaskIdAndStatus(Long taskId, TaskStatus status);
}
