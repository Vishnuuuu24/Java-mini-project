package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseLifecycleService {

    private final CourseRepository courseRepository;
    private final CustomUserRepository customUserRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final GpaService gpaService;
    private final ProfileBusinessValidationService validationService;

    public CourseLifecycleService(
        CourseRepository courseRepository,
        CustomUserRepository customUserRepository,
        FacultyRepository facultyRepository,
        StudentRepository studentRepository,
        GradeRepository gradeRepository,
        GpaService gpaService,
        ProfileBusinessValidationService validationService
    ) {
        this.courseRepository = courseRepository;
        this.customUserRepository = customUserRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.gpaService = gpaService;
        this.validationService = validationService;
    }

    @Transactional
    public Course saveCourseWithFacultyAssignments(Course course, Set<Long> facultyUserIds) {
        if (course == null) {
            throw new ValidationException("Course is required");
        }

        Course persistedCourse = courseRepository.save(course);
        return syncFacultyAssignmentsInternal(persistedCourse, facultyUserIds);
    }

    @Transactional
    public Course syncFacultyAssignmentsForCourse(Long courseId, Set<Long> facultyUserIds) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        return syncFacultyAssignmentsInternal(course, facultyUserIds);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        List<Student> affectedStudents = studentRepository.findDistinctByCourses_Id(courseId);

        for (Student student : affectedStudents) {
            student.getCourses().remove(course);
            studentRepository.save(student);
            gradeRepository.deleteByStudentIdAndCourseId(student.getId(), courseId);
            gpaService.recomputeAndPersistGpa(student);
        }

        courseRepository.delete(course);
    }

    private Course syncFacultyAssignmentsInternal(Course course, Set<Long> facultyUserIds) {
        Set<Long> normalizedFacultyUserIds = facultyUserIds == null
            ? Set.of()
            : facultyUserIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<CustomUser> facultyUsers = normalizedFacultyUserIds.stream()
            .map(this::loadFacultyUser)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Faculty> existingFacultyMembers = new LinkedHashSet<>(course.getFacultyMembers());

        Set<Faculty> targetFacultyMembers = facultyUsers.stream()
            .map(this::ensureFacultyProfile)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Faculty targetFaculty : targetFacultyMembers) {
            targetFaculty.getCourses().add(course);
            facultyRepository.save(targetFaculty);
        }

        for (Faculty existingFaculty : existingFacultyMembers) {
            if (!targetFacultyMembers.contains(existingFaculty)) {
                existingFaculty.getCourses().remove(course);
                facultyRepository.save(existingFaculty);
            }
        }

        course.getFacultyUsers().clear();
        course.getFacultyUsers().addAll(facultyUsers);

        course.getFacultyMembers().clear();
        course.getFacultyMembers().addAll(targetFacultyMembers);

        return courseRepository.save(course);
    }

    private CustomUser loadFacultyUser(Long userId) {
        CustomUser user = customUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (user.getRole() != UserRole.FACULTY) {
            throw new ValidationException("User is not a faculty account: " + userId);
        }

        return user;
    }

    private Faculty ensureFacultyProfile(CustomUser facultyUser) {
        return facultyRepository.findByUserId(facultyUser.getId())
            .map(existingFaculty -> {
                validationService.validateFacultyId(existingFaculty.getFacultyId());
                return existingFaculty;
            })
            .orElseGet(() -> createFacultyProfile(facultyUser));
    }

    private Faculty createFacultyProfile(CustomUser facultyUser) {
        Faculty faculty = new Faculty();
        faculty.setUser(facultyUser);
        faculty.setFacultyId(nextAvailableFacultyId());
        faculty.setDepartment("");
        faculty.setDesignation("");
        faculty.setEmail(facultyUser.getEmail() == null ? "" : facultyUser.getEmail());
        faculty.setPhone("");

        validationService.validateFacultyId(faculty.getFacultyId());
        return facultyRepository.save(faculty);
    }

    private Integer nextAvailableFacultyId() {
        Set<Integer> usedFacultyIds = facultyRepository.findAll().stream()
            .map(Faculty::getFacultyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (int candidate = 100; candidate <= 999; candidate++) {
            if (!usedFacultyIds.contains(candidate)) {
                return candidate;
            }
        }

        throw new ValidationException("No available faculty ids in range 100-999");
    }
}
