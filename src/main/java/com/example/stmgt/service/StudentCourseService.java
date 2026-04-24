package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentCourseService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;
    private final GpaService gpaService;

    public StudentCourseService(
        StudentRepository studentRepository,
        CourseRepository courseRepository,
        GradeRepository gradeRepository,
        GpaService gpaService
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
        this.gpaService = gpaService;
    }

    @Transactional
    public void assignCourseToStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        if (student.getLevel() == null || course.getLevel() == null || student.getLevel() != course.getLevel()) {
            String courseLevel = course.getLevel() == null ? "UNKNOWN" : course.getLevel().getValue();
            String studentLevel = student.getLevel() == null ? "UNKNOWN" : student.getLevel().getValue();
            throw new ValidationException("Cannot assign " + courseLevel
                + " course to " + studentLevel + " student");
        }

        student.getCourses().add(course);
        studentRepository.save(student);
    }

    @Transactional
    public void removeCourseFromStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        student.getCourses().remove(course);
        studentRepository.save(student);

        gradeRepository.deleteByStudentIdAndCourseId(studentId, courseId);
        gpaService.recomputeAndPersistGpa(student);
    }
}
