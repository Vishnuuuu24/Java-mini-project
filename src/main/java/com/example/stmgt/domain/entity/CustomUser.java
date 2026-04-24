package com.example.stmgt.domain.entity;

import com.example.stmgt.domain.converter.UserRoleConverter;
import com.example.stmgt.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "users_customuser",
    indexes = {
        @Index(name = "idx_users_customuser_role", columnList = "role")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class CustomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 150)
    @ToString.Include
    private String username;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName = "";

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName = "";

    @Column(name = "email", nullable = false, length = 254)
    private String email = "";

    @Column(name = "is_staff", nullable = false)
    private boolean isStaff = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_superuser", nullable = false)
    private boolean isSuperuser = false;

    @Column(name = "date_joined", nullable = false)
    private OffsetDateTime dateJoined;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Convert(converter = UserRoleConverter.class)
    @Column(name = "role", nullable = false, length = 10)
    @ToString.Include
    private UserRole role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Student studentProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Faculty facultyProfile;

    @ManyToMany(mappedBy = "facultyUsers", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Course> teachingCourses = new HashSet<>();

    @ManyToMany(mappedBy = "assignedStudents", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Task> assignedTasks = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Task> createdTasks = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<TaskProgress> taskProgressRecords = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "users_customuser_groups",
        joinColumns = @JoinColumn(
            name = "customuser_id",
            foreignKey = @ForeignKey(name = "fk_customuser_groups_user")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "group_id",
            foreignKey = @ForeignKey(name = "fk_customuser_groups_group")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_customuser_groups_pair", columnNames = {"customuser_id", "group_id"})
        }
    )
    @ToString.Exclude
    private Set<AuthGroup> groups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "users_customuser_user_permissions",
        joinColumns = @JoinColumn(
            name = "customuser_id",
            foreignKey = @ForeignKey(name = "fk_customuser_permissions_user")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "permission_id",
            foreignKey = @ForeignKey(name = "fk_customuser_permissions_permission")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_customuser_permissions_pair", columnNames = {"customuser_id", "permission_id"})
        }
    )
    @ToString.Exclude
    private Set<AuthPermission> userPermissions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (dateJoined == null) {
            dateJoined = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

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
        CustomUser that = (CustomUser) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
