package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.enums.UserRole;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.CustomUserRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.service.exception.NotFoundException;
import com.example.stmgt.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FacultyLifecycleService {

    private final FacultyRepository facultyRepository;
    private final CustomUserRepository customUserRepository;
    private final CourseRepository courseRepository;
    private final ProfileBusinessValidationService profileBusinessValidationService;

    public FacultyLifecycleService(
        FacultyRepository facultyRepository,
        CustomUserRepository customUserRepository,
        CourseRepository courseRepository,
        ProfileBusinessValidationService profileBusinessValidationService
    ) {
        this.facultyRepository = facultyRepository;
        this.customUserRepository = customUserRepository;
        this.courseRepository = courseRepository;
        this.profileBusinessValidationService = profileBusinessValidationService;
    }

    @Transactional
    public Faculty createFacultyProfile(
        Long userId,
        Integer facultyId,
        String department,
        String designation,
        String email,
        String phone,
        Set<Long> courseIds
    ) {
        if (userId == null) {
            throw new ValidationException("Faculty user id is required");
        }

        profileBusinessValidationService.validateFacultyId(facultyId);

        CustomUser user = customUserRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Selected user no longer exists"));

        if (user.getRole() != UserRole.FACULTY) {
            throw new ValidationException("Selected user is not a faculty account");
        }

        if (facultyRepository.existsByUserId(user.getId())) {
            throw new ValidationException("Faculty profile already exists for this user");
        }

        if (facultyId != null && facultyRepository.existsByFacultyId(facultyId)) {
            throw new ValidationException("Faculty id already exists: " + facultyId);
        }

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setFacultyId(facultyId);
        faculty.setDepartment(defaultString(department));
        faculty.setDesignation(defaultString(designation));
        faculty.setEmail(user.getEmail() == null ? defaultString(email) : user.getEmail());
        faculty.setPhone(defaultString(phone));

        if (courseIds != null && !courseIds.isEmpty()) {
            Set<Long> normalizedCourseIds = new HashSet<>(courseIds);
            List<Course> courses = courseRepository.findAllById(normalizedCourseIds);
            if (courses.size() != normalizedCourseIds.size()) {
                throw new ValidationException("One or more selected courses no longer exist");
            }
            faculty.getCourses().addAll(courses);
        }

        return facultyRepository.save(faculty);
    }

    @Transactional
    public String assignCourseToFaculty(Long facultyId, Long courseId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new NotFoundException("Faculty not found: " + facultyId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        faculty.getCourses().add(course);
        facultyRepository.save(faculty);

        return "Course assigned to " + faculty.getUser().getUsername() + " successfully.";
    }

    @Transactional
    public String updateFacultyProfile(
        Long facultyId,
        Integer updatedFacultyId,
        String department,
        String designation,
        String phone,
        String firstName,
        String lastName,
        String email
    ) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new NotFoundException("Faculty not found: " + facultyId));

        profileBusinessValidationService.validateFacultyId(updatedFacultyId);
        if (updatedFacultyId != null && facultyRepository.existsByFacultyIdAndIdNot(updatedFacultyId, faculty.getId())) {
            throw new ValidationException("Faculty id already exists: " + updatedFacultyId);
        }

        CustomUser user = faculty.getUser();
        user.setFirstName(defaultString(firstName));
        user.setLastName(defaultString(lastName));
        customUserRepository.save(user);

        faculty.setFacultyId(updatedFacultyId);
        faculty.setDepartment(defaultString(department));
        faculty.setDesignation(defaultString(designation));
        faculty.setPhone(defaultString(phone));
        faculty.setEmail(defaultString(email).isBlank() ? user.getEmail() : defaultString(email));
        facultyRepository.save(faculty);

        return "Details for " + user.getUsername() + " have been updated.";
    }

    @Transactional
    public String removeCourseFromFaculty(Long facultyId, Long courseId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new NotFoundException("Faculty not found: " + facultyId));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        faculty.getCourses().remove(course);
        facultyRepository.save(faculty);

        return "Course " + course.getName() + " has been removed from " + faculty.getUser().getUsername();
    }

    @Transactional
    public void deleteFacultyProfile(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
            .orElseThrow(() -> new NotFoundException("Faculty not found: " + facultyId));

        Long linkedUserId = faculty.getUser() == null ? null : faculty.getUser().getId();
        if (linkedUserId == null) {
            facultyRepository.delete(faculty);
            return;
        }

        CustomUser linkedUser = customUserRepository.findById(linkedUserId).orElse(null);
        if (linkedUser == null) {
            facultyRepository.delete(faculty);
            return;
        }

        if (linkedUser.getRole() == UserRole.FACULTY) {
            customUserRepository.delete(linkedUser);
            return;
        }

        facultyRepository.delete(faculty);
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}
