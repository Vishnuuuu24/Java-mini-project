package com.example.stmgt.domain.entity;

import com.example.stmgt.domain.converter.AcademicLevelConverter;
import com.example.stmgt.domain.enums.AcademicLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "student_student",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_register_no", columnNames = "register_no")
    },
    indexes = {
        @Index(name = "idx_student_level", columnList = "level")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_student_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private CustomUser user;

    @Column(name = "register_no", nullable = false, unique = true, length = 12)
    @ToString.Include
    private String registerNo;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "attendance", nullable = false)
    private Double attendance = 0.0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_student_courses",
        joinColumns = @JoinColumn(
            name = "student_id",
            foreignKey = @ForeignKey(name = "fk_student_courses_student")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "course_id",
            foreignKey = @ForeignKey(name = "fk_student_courses_course")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_student_courses_pair", columnNames = {"student_id", "course_id"})
        }
    )
    @ToString.Exclude
    private Set<Course> courses = new HashSet<>();

    @Column(name = "gpa", nullable = false)
    private Double gpa = 0.0;

    @Convert(converter = AcademicLevelConverter.class)
    @Column(name = "level", length = 20)
    private AcademicLevel level;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Grade> grades = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Student that = (Student) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
