package com.example.stmgt.domain.entity;

import com.example.stmgt.domain.converter.TaskPriorityConverter;
import com.example.stmgt.domain.converter.TaskStatusConverter;
import com.example.stmgt.domain.enums.TaskPriority;
import com.example.stmgt.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
    name = "task_task",
    indexes = {
        @Index(name = "idx_task_created_by", columnList = "created_by_id"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_due_date", columnList = "due_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    @ToString.Include
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_task_assigned_to",
        joinColumns = @JoinColumn(
            name = "task_id",
            foreignKey = @ForeignKey(name = "fk_task_assigned_task")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "customuser_id",
            foreignKey = @ForeignKey(name = "fk_task_assigned_user")
        ),
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_task_assigned_pair", columnNames = {"task_id", "customuser_id"})
        }
    )
    @ToString.Exclude
    private Set<CustomUser> assignedStudents = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by_id",
        foreignKey = @ForeignKey(name = "fk_task_created_by")
    )
    @ToString.Exclude
    private CustomUser createdBy;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Convert(converter = TaskStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Convert(converter = TaskPriorityConverter.class)
    @Column(name = "priority", length = 10)
    private TaskPriority priority;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<TaskProgress> studentProgress = new HashSet<>();

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
        Task that = (Task) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
