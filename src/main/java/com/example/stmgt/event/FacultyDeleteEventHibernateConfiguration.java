package com.example.stmgt.event;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class FacultyDeleteEventHibernateConfiguration implements HibernatePropertiesCustomizer {

    private final FacultyEntityDeletedHibernateListener facultyDeleteListener;

    public FacultyDeleteEventHibernateConfiguration(FacultyEntityDeletedHibernateListener facultyDeleteListener) {
        this.facultyDeleteListener = facultyDeleteListener;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.integrator_provider", (IntegratorProvider) () -> List.of(
            new Integrator() {
                @Override
                public void integrate(
                    Metadata metadata,
                    SessionFactoryImplementor sessionFactory,
                    SessionFactoryServiceRegistry serviceRegistry
                ) {
                    EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
                    eventListenerRegistry.appendListeners(EventType.POST_DELETE, facultyDeleteListener);
                }

                @Override
                public void disintegrate(
                    SessionFactoryImplementor sessionFactory,
                    SessionFactoryServiceRegistry serviceRegistry
                ) {
                    // No-op
                }
            }
        ));
    }
}
