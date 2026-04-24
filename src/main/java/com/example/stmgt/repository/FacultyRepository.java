package com.example.stmgt.repository;

import com.example.stmgt.domain.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByFacultyId(Integer facultyId);

    boolean existsByFacultyIdAndIdNot(Integer facultyId, Long id);

    List<Faculty> findByCourses_Id(Long courseId);
}
