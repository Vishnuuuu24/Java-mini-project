package com.example.stmgt.domain.entity;

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
    name = "auth_group",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_name", columnNames = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class AuthGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    @ToString.Include
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "auth_group_permissions",
        joinColumns = @JoinColumn(
            name = "group_id",
            foreignKey = @ForeignKey(name = "fk_group_permissions_group")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "permission_id",
            foreignKey = @ForeignKey(name = "fk_group_permissions_permission")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_group_permissions_pair", columnNames = {"group_id", "permission_id"})
        }
    )
    @ToString.Exclude
    private Set<AuthPermission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<CustomUser> users = new HashSet<>();

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
        AuthGroup that = (AuthGroup) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
