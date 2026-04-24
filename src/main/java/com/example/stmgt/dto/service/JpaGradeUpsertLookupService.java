package com.example.stmgt.dto.service;

import com.example.stmgt.domain.entity.Course;
import com.example.stmgt.domain.entity.Grade;
import com.example.stmgt.domain.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class JpaGradeUpsertLookupService implements GradeUpsertLookupService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public boolean studentExists(Long studentId) {
        Long count = entityManager.createQuery(
                "select count(s.id) from Student s where s.id = :studentId",
                Long.class
            )
            .setParameter("studentId", studentId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseExists(Long courseId) {
        Long count = entityManager.createQuery(
                "select count(c.id) from Course c where c.id = :courseId",
                Long.class
            )
            .setParameter("courseId", courseId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
        Long count = entityManager.createQuery(
                "select count(s.id) from Student s join s.courses c where s.id = :studentId and c.id = :courseId",
                Long.class
            )
            .setParameter("studentId", studentId)
            .setParameter("courseId", courseId)
            .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId) {
        return entityManager.createQuery(
                "select g from Grade g where g.student.id = :studentId and g.course.id = :courseId",
                Grade.class
            )
            .setParameter("studentId", studentId)
            .setParameter("courseId", courseId)
            .setMaxResults(1)
            .getResultStream()
            .findFirst();
    }

    @Override
    public Student getStudentReference(Long studentId) {
        return entityManager.getReference(Student.class, studentId);
    }

    @Override
    public Course getCourseReference(Long courseId) {
        return entityManager.getReference(Course.class, courseId);
    }

    @Override
    @Transactional
    public Grade save(Grade grade) {
        if (grade.getId() == null) {
            entityManager.persist(grade);
            return grade;
        }

        return entityManager.merge(grade);
    }
}
