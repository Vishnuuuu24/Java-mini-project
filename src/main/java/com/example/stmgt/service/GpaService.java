package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GpaService {

    private static final Map<String, Integer> GRADE_POINTS = Map.of(
        "A", 10,
        "B", 8,
        "C", 6,
        "D", 4,
        "F", 0
    );

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;

    public GpaService(
        GradeRepository gradeRepository,
        StudentRepository studentRepository
    ) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public double calculateGpa(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return calculateFromGrades(grades);
    }

    @Transactional(readOnly = true)
    public double calculateGpa(Student student) {
        if (student.getId() == null) {
            return 0.0;
        }
        List<Grade> grades = gradeRepository.findByStudentId(student.getId());
        return calculateFromGrades(grades);
    }

    @Transactional
    public double recomputeAndPersistGpa(Long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
        return recomputeAndPersistGpa(student);
    }

    @Transactional
    public double recomputeAndPersistGpa(Student student) {
        double gpa = calculateGpa(student);
        student.setGpa(gpa);
        studentRepository.save(student);
        return gpa;
    }

    private double calculateFromGrades(List<Grade> grades) {
        if (grades.isEmpty()) {
            return 0.0;
        }

        int totalPoints = 0;
        for (Grade grade : grades) {
            String normalizedGrade = normalizeGrade(grade.getGrade());
            totalPoints += GRADE_POINTS.getOrDefault(normalizedGrade, 0);
        }

        return Math.round(((double) totalPoints / grades.size()) * 100.0d) / 100.0d;
    }

    private String normalizeGrade(String grade) {
        if (grade == null) {
            return null;
        }
        return grade.trim().toUpperCase();
    }
}
