package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(BaseEntity.BaseEntityListener.class)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static class BaseEntityListener {

        @PrePersist
        void onPrePersist(Object entity) {
            LocalDateTime now = LocalDateTime.now();
            if (entity instanceof BaseEntity be) {
                if (be.createdAt == null) {
                    be.createdAt = now;
                }
                if (be.isDeleted == null) {
                    be.isDeleted = false;
                }
            }
        }

        @PreUpdate
        void onPreUpdate(Object entity) {
            if (entity instanceof BaseEntity be) {
                be.updatedAt = LocalDateTime.now();
            }
        }
    }
}
