package ats.entity;

import ats.constant.JobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "jobs")
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE jobs SET is_deleted = 1 WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Job extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department departmentId;

    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private User recruiterId;

    @Column(name = "title", columnDefinition = "VARCHAR(500)")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", columnDefinition = "VARCHAR(500)")
    private String location;

    @Column(name = "salary_min", columnDefinition = "NUMERIC(15,2)")
    private BigDecimal salaryMin;

    @Column(name = "salary_max", columnDefinition = "NUMERIC(15,2)")
    private BigDecimal salaryMax;

    @Column(name = "status", columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private JobStatus status;
}
