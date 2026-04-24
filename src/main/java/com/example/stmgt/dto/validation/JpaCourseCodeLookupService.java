package com.example.stmgt.dto.validation;

import com.example.stmgt.domain.entity.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JpaCourseCodeLookupService implements CourseCodeLookupService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        Long count = entityManager.createQuery(
                "select count(c.id) from Course c where c.code = :code",
                Long.class
            )
            .setParameter("code", code.trim())
            .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsByCodeExcludingCourseId(String code, Long excludedCourseId) {
        if (code == null || code.isBlank()) {
            return false;
        }

        Long count = entityManager.createQuery(
                "select count(c.id) from Course c where c.code = :code and c.id <> :excludedCourseId",
                Long.class
            )
            .setParameter("code", code.trim())
            .setParameter("excludedCourseId", excludedCourseId)
            .getSingleResult();
        return count > 0;
    }
}
