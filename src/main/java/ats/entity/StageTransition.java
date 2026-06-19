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

import java.time.LocalDateTime;

@Entity
@Table(name = "stage_transitions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class StageTransition extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application applicationId;

    @ManyToOne
    @JoinColumn(name = "from_stage_id")
    private PipelineStage fromStageId;

    @ManyToOne
    @JoinColumn(name = "to_stage_id")
    private PipelineStage toStageId;

    private String notes;

    @Column(name = "moved_at")
    private LocalDateTime movedAt;

}
