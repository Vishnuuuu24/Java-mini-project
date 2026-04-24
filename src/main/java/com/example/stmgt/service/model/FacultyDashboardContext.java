package com.example.stmgt.service.model;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Faculty;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Task;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FacultyDashboardContext {

    private final Faculty faculty;
    private final List<Course> courses;
    private final long studentsCount;
    private final List<Task> tasks;
    private final long pendingGrades;
    private final List<Grade> pendingGradesList;
    private final List<Task> recentTasks;
    private final String facultyName;
    private final Map<Long, Long> pendingGradesByCourseId;

    public FacultyDashboardContext(
        Faculty faculty,
        List<Course> courses,
        long studentsCount,
        List<Task> tasks,
        long pendingGrades,
        List<Grade> pendingGradesList,
        List<Task> recentTasks,
        String facultyName,
        Map<Long, Long> pendingGradesByCourseId
    ) {
        this.faculty = faculty;
        this.courses = courses;
        this.studentsCount = studentsCount;
        this.tasks = tasks;
        this.pendingGrades = pendingGrades;
        this.pendingGradesList = pendingGradesList;
        this.recentTasks = recentTasks;
        this.facultyName = facultyName;
        this.pendingGradesByCourseId = pendingGradesByCourseId;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public long getStudentsCount() {
        return studentsCount;
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public long getPendingGrades() {
        return pendingGrades;
    }

    public List<Grade> getPendingGradesList() {
        return Collections.unmodifiableList(pendingGradesList);
    }

    public List<Task> getRecentTasks() {
        return Collections.unmodifiableList(recentTasks);
    }

    public String getFacultyName() {
        return facultyName;
    }

    public Map<Long, Long> getPendingGradesByCourseId() {
        return Collections.unmodifiableMap(pendingGradesByCourseId);
    }
}
