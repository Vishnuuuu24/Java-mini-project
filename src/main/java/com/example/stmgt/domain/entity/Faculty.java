package com.example.stmgt.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "faculty_faculty",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_faculty_faculty_id", columnNames = "faculty_id")
    },
    indexes = {
        @Index(name = "idx_faculty_department", columnList = "department")
    }
)
@Check(constraints = "faculty_id is null or (faculty_id between 100 and 999)")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_faculty_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private CustomUser user;

    @Min(100)
    @Max(999)
    @Column(name = "faculty_id", unique = true)
    @ToString.Include
    private Integer facultyId;

    @Column(name = "department", nullable = false, length = 100)
    private String department = "";

    @Column(name = "designation", nullable = false, length = 100)
    private String designation = "";

    @Column(name = "email", nullable = false, length = 100)
    private String email = "";

    @Column(name = "phone", nullable = false, length = 15)
    private String phone = "";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "faculty_faculty_courses",
        joinColumns = @JoinColumn(
            name = "faculty_id",
            foreignKey = @ForeignKey(name = "fk_faculty_courses_faculty")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "course_id",
            foreignKey = @ForeignKey(name = "fk_faculty_courses_course")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_faculty_courses_pair", columnNames = {"faculty_id", "course_id"})
        }
    )
    @ToString.Exclude
    private Set<Course> courses = new HashSet<>();

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
        Faculty that = (Faculty) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
