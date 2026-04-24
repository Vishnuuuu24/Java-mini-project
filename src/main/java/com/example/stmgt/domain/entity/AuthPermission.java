package com.example.stmgt.domain.entity;

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
    name = "auth_permission",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_content_type_codename", columnNames = {"content_type_id", "codename"})
    },
    indexes = {
        @Index(name = "idx_permission_content_type", columnList = "content_type_id"),
        @Index(name = "idx_permission_codename", columnList = "codename")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class AuthPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    @ToString.Include
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "content_type_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_permission_content_type")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private DjangoContentType contentType;

    @Column(name = "codename", nullable = false, length = 100)
    @ToString.Include
    private String codename;

    @ManyToMany(mappedBy = "userPermissions", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<CustomUser> users = new HashSet<>();

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<AuthGroup> groups = new HashSet<>();

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
        AuthPermission that = (AuthPermission) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
