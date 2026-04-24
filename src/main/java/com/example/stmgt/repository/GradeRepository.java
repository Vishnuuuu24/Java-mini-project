package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByCourseId(Long courseId);

    List<Grade> findByCourseIdIn(Collection<Long> courseIds);

    long countByCourseIdAndGradeIsNull(Long courseId);

    long countByCourseIdInAndGradeIsNull(Collection<Long> courseIds);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}
