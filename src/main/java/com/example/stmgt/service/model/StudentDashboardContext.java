package com.example.stmgt.service.model;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import com.example.stmgt.domain.entity.Task;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StudentDashboardContext {

    private final Student student;
    private final List<Grade> grades;
    private final Set<Course> courses;
    private final double gpa;
    private final List<Task> tasks;
    private final List<Task> pendingTasks;
    private final List<Task> completedTasks;

    public StudentDashboardContext(
        Student student,
        List<Grade> grades,
        Set<Course> courses,
        double gpa,
        List<Task> tasks,
        List<Task> pendingTasks,
        List<Task> completedTasks
    ) {
        this.student = student;
        this.grades = grades;
        this.courses = courses;
        this.gpa = gpa;
        this.tasks = tasks;
        this.pendingTasks = pendingTasks;
        this.completedTasks = completedTasks;
    }

    public static StudentDashboardContext empty() {
        return new StudentDashboardContext(
            null,
            List.of(),
            Set.of(),
            0.0,
            List.of(),
            List.of(),
            List.of()
        );
    }

    public Student getStudent() {
        return student;
    }

    public List<Grade> getGrades() {
        return Collections.unmodifiableList(grades);
    }

    public Set<Course> getCourses() {
        return Collections.unmodifiableSet(courses);
    }

    public double getGpa() {
        return gpa;
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<Task> getPendingTasks() {
        return Collections.unmodifiableList(pendingTasks);
    }

    public List<Task> getCompletedTasks() {
        return Collections.unmodifiableList(completedTasks);
    }
}
