package com.example.stmgt.service.model;

import com.example.stmgt.domain.enums.TaskStatus;

public class TaskProgressSummary {

    private final long pendingCount;
    private final long inProgressCount;
    private final long completedCount;
    private final long totalAssignedStudents;
    private final TaskStatus overallStatus;

    public TaskProgressSummary(
        long pendingCount,
        long inProgressCount,
        long completedCount,
        long totalAssignedStudents,
        TaskStatus overallStatus
    ) {
        this.pendingCount = pendingCount;
        this.inProgressCount = inProgressCount;
        this.completedCount = completedCount;
        this.totalAssignedStudents = totalAssignedStudents;
        this.overallStatus = overallStatus;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public long getTotalAssignedStudents() {
        return totalAssignedStudents;
    }

    public TaskStatus getOverallStatus() {
        return overallStatus;
    }
}
