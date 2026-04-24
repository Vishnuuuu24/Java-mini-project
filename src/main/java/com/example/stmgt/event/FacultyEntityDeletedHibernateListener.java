package com.example.stmgt.event;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.domain.entity.Faculty;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FacultyEntityDeletedHibernateListener implements PostDeleteEventListener {

    private final FacultyDomainEventPublisher eventPublisher;

    public FacultyEntityDeletedHibernateListener(FacultyDomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (!(event.getEntity() instanceof Faculty faculty)) {
            return;
        }

        Long facultyId = faculty.getId();
        Long linkedUserId = resolveLinkedUserId(event, faculty);

        eventPublisher.publishFacultyProfileDeleted(facultyId, linkedUserId);
    }

    private Long resolveLinkedUserId(PostDeleteEvent event, Faculty faculty) {
        if (faculty.getUser() != null && faculty.getUser().getId() != null) {
            return faculty.getUser().getId();
        }

        String[] propertyNames = event.getPersister().getPropertyNames();
        Object[] deletedState = event.getDeletedState();

        for (int i = 0; i < propertyNames.length; i++) {
            if (Objects.equals("user", propertyNames[i])) {
                return extractUserId(deletedState[i]);
            }
        }

        return null;
    }

    private Long extractUserId(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof CustomUser customUser) {
            return customUser.getId();
        }

        if (value instanceof HibernateProxy proxy) {
            Object identifier = proxy.getHibernateLazyInitializer().getIdentifier();
            if (identifier instanceof Long id) {
                return id;
            }
        }

        return null;
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }
}
