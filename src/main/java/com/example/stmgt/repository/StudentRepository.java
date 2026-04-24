package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByRegisterNo(String registerNo);

    boolean existsByRegisterNoAndIdNot(String registerNo, Long id);

    List<Student> findDistinctByCourses_Id(Long courseId);

    List<Student> findDistinctByCoursesIn(Collection<Course> courses);
}
