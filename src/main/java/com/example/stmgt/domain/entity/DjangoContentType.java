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
    name = "django_content_type",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_type_app_model", columnNames = {"app_label", "model"})
    },
    indexes = {
        @Index(name = "idx_content_type_app_label", columnList = "app_label"),
        @Index(name = "idx_content_type_model", columnList = "model")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class DjangoContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "app_label", nullable = false, length = 100)
    @ToString.Include
    private String appLabel;

    @Column(name = "model", nullable = false, length = 100)
    @ToString.Include
    private String model;

    @OneToMany(mappedBy = "contentType", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<AuthPermission> permissions = new HashSet<>();

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
        DjangoContentType that = (DjangoContentType) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
