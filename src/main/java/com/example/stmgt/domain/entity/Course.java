package com.example.stmgt.domain.entity;

import com.example.stmgt.domain.converter.AcademicLevelConverter;
import com.example.stmgt.domain.enums.AcademicLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "courses_course",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_code", columnNames = "code")
    },
    indexes = {
        @Index(name = "idx_course_level", columnList = "level")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    @ToString.Include
    private String code;

    @Convert(converter = AcademicLevelConverter.class)
    @Column(name = "level", nullable = false, length = 20)
    private AcademicLevel level;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "courses_course_faculty",
        joinColumns = @JoinColumn(
            name = "course_id",
            foreignKey = @ForeignKey(name = "fk_course_faculty_course")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "customuser_id",
            foreignKey = @ForeignKey(name = "fk_course_faculty_user")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_course_faculty_pair", columnNames = {"course_id", "customuser_id"})
        }
    )
    @ToString.Exclude
    private Set<CustomUser> facultyUsers = new HashSet<>();

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Faculty> facultyMembers = new HashSet<>();

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
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
        Course that = (Course) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
