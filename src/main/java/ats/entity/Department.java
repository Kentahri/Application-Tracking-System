package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "departments")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter @Setter
@SQLDelete(sql = "UPDATE departments SET is_deleted = 1, update_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Department extends BaseEntity{

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "department_name", columnDefinition = "NVARCHAR(255)")
    private String departmentName;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;
}
