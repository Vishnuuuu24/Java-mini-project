package com.example.stmgt.service.model;

public class AdminSummary {

    private final long totalStudents;
    private final long totalFaculty;
    private final long totalTasks;
    private final long totalCourses;

    public AdminSummary(long totalStudents, long totalFaculty, long totalTasks, long totalCourses) {
        this.totalStudents = totalStudents;
        this.totalFaculty = totalFaculty;
        this.totalTasks = totalTasks;
        this.totalCourses = totalCourses;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getTotalCourses() {
        return totalCourses;
    }
}
