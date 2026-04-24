package com.example.stmgt.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class FacultyDomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public FacultyDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishFacultyProfileDeleted(Long facultyId, Long linkedUserId) {
        applicationEventPublisher.publishEvent(new FacultyProfileDeletedEvent(facultyId, linkedUserId));
    }
}
