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

@Entity
@Table(name = "applications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
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
    @JoinColumn(name = "pineline_stage_id")
    private PinelineStage pinelineStageId;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String status;

}