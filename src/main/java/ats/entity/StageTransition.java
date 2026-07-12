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
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "stage_transitions")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE stage_transitions SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
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

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String notes;

    @Column(name = "moved_at")
    private LocalDateTime movedAt;

}
