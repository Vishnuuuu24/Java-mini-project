package com.example.stmgt.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.entity.Task;
import com.example.stmgt.domain.enums.TaskStatus;
import com.example.stmgt.repository.CourseRepository;
import com.example.stmgt.repository.FacultyRepository;
import com.example.stmgt.repository.GradeRepository;
import com.example.stmgt.repository.StudentRepository;
import com.example.stmgt.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stmgt.service.model.AdminSummary;
import com.example.stmgt.service.model.FacultyDashboardContext;
import com.example.stmgt.service.model.StudentDashboardContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;
    private final GradeManagementService gradeManagementService;

    public DashboardService(
        StudentRepository studentRepository,
        FacultyRepository facultyRepository,
        TaskRepository taskRepository,
        CourseRepository courseRepository,
        GradeRepository gradeRepository,
        GradeManagementService gradeManagementService
    ) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
        this.gradeManagementService = gradeManagementService;
    }

    @Transactional(readOnly = true)
    public AdminSummary getAdminSummary() {
        return new AdminSummary(
            studentRepository.count(),
            facultyRepository.count(),
            taskRepository.count(),
            courseRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public StudentDashboardContext getStudentDashboardContext(Long studentUserId) {
        return studentRepository.findByUserId(studentUserId)
            .map(student -> {
                List<Grade> grades = gradeRepository.findByStudentId(student.getId());
                List<Task> tasks = taskRepository.findByAssignedStudentsId(studentUserId);

                List<Task> pendingTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.PENDING)
                    .collect(Collectors.toList());

                List<Task> completedTasks = tasks.stream()
                    .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                    .collect(Collectors.toList());

                return new StudentDashboardContext(
                    student,
                    grades,
                    new HashSet<>(student.getCourses()),
                    student.getGpa() == null ? 0.0 : student.getGpa(),
                    tasks,
                    pendingTasks,
                    completedTasks
                );
            })
            .orElse(StudentDashboardContext.empty());
    }

    @Transactional
    public FacultyDashboardContext getFacultyDashboardContext(Long facultyUserId) {
        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
            .orElseThrow(() -> new com.example.stmgt.service.exception.NotFoundException(
                "Faculty profile not found for user: " + facultyUserId
            ));

        List<Course> facultyCourses = new ArrayList<>(faculty.getCourses());
        Set<Long> distinctStudentIds = new HashSet<>();
        Map<Long, Long> pendingGradesByCourseId = new HashMap<>();

        for (Course course : facultyCourses) {
            List<Student> studentsInCourse = studentRepository.findDistinctByCourses_Id(course.getId());
            for (Student student : studentsInCourse) {
                distinctStudentIds.add(student.getId());
                gradeManagementService.ensureGradePlaceholder(student.getId(), course.getId());
            }

            long pendingCount = gradeRepository.countByCourseIdAndGradeIsNull(course.getId());
            if (pendingCount > 0) {
                pendingGradesByCourseId.put(course.getId(), pendingCount);
            }
        }

        List<Long> courseIds = facultyCourses.stream().map(Course::getId).collect(Collectors.toList());

        List<Grade> pendingGradeRecords = courseIds.isEmpty()
            ? List.of()
            : gradeRepository.findByCourseIdIn(courseIds).stream()
                .filter(grade -> grade.getGrade() == null)
                .collect(Collectors.toList());

        List<Task> facultyTasks = taskRepository.findByCreatedByIdOrderByDueDateDesc(facultyUserId);
        List<Task> recentTasks = facultyTasks.stream().limit(5).collect(Collectors.toList());

        String firstName = faculty.getUser().getFirstName() == null ? "" : faculty.getUser().getFirstName();
        String lastName = faculty.getUser().getLastName() == null ? "" : faculty.getUser().getLastName();
        String facultyName = (firstName + " " + lastName).trim();

        return new FacultyDashboardContext(
            faculty,
            facultyCourses,
            distinctStudentIds.size(),
            facultyTasks,
            pendingGradeRecords.size(),
            pendingGradeRecords,
            recentTasks,
            facultyName,
            pendingGradesByCourseId
        );
    }
}
