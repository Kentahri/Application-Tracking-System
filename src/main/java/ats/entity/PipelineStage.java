package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pipeline_stages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PipelineStage extends BaseEntity{

    @Column(name = "stage_name", columnDefinition = "NVARCHAR(100)")
    private String stageName;

    @Column(name = "stage_order")
    private Integer stageOrder;

}
