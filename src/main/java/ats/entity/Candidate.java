package ats.entity;

import ats.constant.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "candidates")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@SQLDelete(sql = "UPDATE candidates SET is_deleted = 1, update_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Candidate extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "upgrade_package_id")
    private UpgradePackage upgradePackageId;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "password_hash", columnDefinition = "NVARCHAR(255)")
    private String passwordHash;

    @Column(columnDefinition = "NVARCHAR(30)")
    private String phone;

    @Builder.Default
    @Column(name = "number_of_query_quota", nullable = false)
    private Integer numberOfQueryQuota = 0;

    @Column(columnDefinition = "NVARCHAR(50)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus candidateStatus = UserStatus.ACTIVE;

}
