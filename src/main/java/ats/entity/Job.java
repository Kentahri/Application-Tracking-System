package ats.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "jobs")
@EqualsAndHashCode(callSuper = true)
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private User recruiter;

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
    private String status;

    @Column(name = "utm_source", columnDefinition = "VARCHAR(150)")
    private String utmSource;

    @Column(name = "utm_medium", columnDefinition = "VARCHAR(150)")
    private String utmMedium;

    @Column(name = "deadline", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime deadline;

    @Column(name = "published_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime publishedAt;

}
