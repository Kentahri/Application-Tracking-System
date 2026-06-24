package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "applications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE applications SET is_deleted = 1 WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Application extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job jobId;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidateId;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    private Cv cvId;

    @ManyToOne
    @JoinColumn(name = "pipeline_stage_id")
    private PipelineStage pipelineStageId;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String status;

}
