package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "upgrade_packages")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@SQLDelete(sql = "UPDATE upgrade_packages SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class UpgradePackage extends BaseEntity {

    @Column(name = "package_name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String packageName;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(columnDefinition = "NUMERIC(15,2)", nullable = false)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "number_of_query_quota", nullable = false)
    private Integer numberOfQueryQuota = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer priority = 0;
}
