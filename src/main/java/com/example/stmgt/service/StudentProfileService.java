package com.example.stmgt.service;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.enums.AcademicLevel;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentProfileService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CustomUserRepository customUserRepository;
    private final GpaService gpaService;
    private final ProfileBusinessValidationService profileBusinessValidationService;

    public StudentProfileService(
        StudentRepository studentRepository,
        CourseRepository courseRepository,
        CustomUserRepository customUserRepository,
        GpaService gpaService,
        ProfileBusinessValidationService profileBusinessValidationService
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.customUserRepository = customUserRepository;
        this.gpaService = gpaService;
        this.profileBusinessValidationService = profileBusinessValidationService;
    }

    @Transactional
    public Student createStudentProfile(
        Long userId,
        String registerNo,
        String department,
        Double attendance,
        AcademicLevel level,
        Set<Long> courseIds
    ) {
        if (userId == null) {
            throw new ValidationException("Student user id is required");
        }

        CustomUser user = customUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Selected user no longer exists"));

        if (user.getRole() != UserRole.STUDENT) {
            throw new ValidationException("Selected user is not a student account");
        }

        if (studentRepository.existsByUserId(user.getId())) {
            throw new ValidationException("Student profile already exists for selected user");
        }

        String normalizedRegisterNo = defaultString(registerNo);
        if (studentRepository.existsByRegisterNo(normalizedRegisterNo)) {
            throw new ValidationException("Register number already exists: " + normalizedRegisterNo);
        }

        profileBusinessValidationService.validateStudentAttendance(attendance);
        profileBusinessValidationService.validateStudentLevel(level);

        Student student = new Student();
        student.setUser(user);
        student.setRegisterNo(normalizedRegisterNo);
        student.setDepartment(defaultString(department));
        student.setAttendance(attendance);
        student.setLevel(level);
        student.setGpa(0.0);

        if (courseIds != null && !courseIds.isEmpty()) {
            Set<Long> normalizedCourseIds = new HashSet<>(courseIds);
            List<Course> courses = courseRepository.findAllById(normalizedCourseIds);
            if (courses.size() != normalizedCourseIds.size()) {
                throw new ValidationException("One or more selected courses no longer exist");
            }
            student.getCourses().addAll(courses);
        }

        Student createdStudent = studentRepository.save(student);
        gpaService.recomputeAndPersistGpa(createdStudent);
        return createdStudent;
    }

    @Transactional
    public void deleteStudentProfile(Long studentId) {
        if (studentId == null) {
            throw new ValidationException("Student id is required");
        }

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        if (student.getUser() != null) {
            customUserRepository.delete(student.getUser());
            return;
        }

        studentRepository.delete(student);
    }

    @Transactional
    public Student updateStudentProfile(
        Long studentId,
        String firstName,
        String lastName,
        String registerNo,
        String department,
        Double attendance,
        AcademicLevel level
    ) {
        if (studentId == null) {
            throw new ValidationException("Student id is required");
        }

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        CustomUser user = student.getUser();
        if (user == null) {
            throw new ValidationException("Student profile is not linked to a user account");
        }

        // Mirror Django update_details ordering by persisting user names first.
        user.setFirstName(defaultString(firstName));
        user.setLastName(defaultString(lastName));
        customUserRepository.save(user);

        String normalizedRegisterNo = defaultString(registerNo);
        if (studentRepository.existsByRegisterNoAndIdNot(normalizedRegisterNo, student.getId())) {
            throw new ValidationException("Register number already exists: " + normalizedRegisterNo);
        }

        profileBusinessValidationService.validateStudentAttendance(attendance);
        profileBusinessValidationService.validateStudentLevel(level);

        student.setRegisterNo(normalizedRegisterNo);
        student.setDepartment(defaultString(department));
        student.setAttendance(attendance);
        student.setLevel(level);

        return studentRepository.save(student);
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}
