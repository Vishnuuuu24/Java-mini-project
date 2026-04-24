package com.example.stmgt.event;

public class FacultyProfileDeletedEvent {

    private final Long facultyId;
    private final Long linkedUserId;

    public FacultyProfileDeletedEvent(Long facultyId, Long linkedUserId) {
        this.facultyId = facultyId;
        this.linkedUserId = linkedUserId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public Long getLinkedUserId() {
        return linkedUserId;
    }
}
