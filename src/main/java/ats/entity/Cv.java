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
@Table(name = "cvs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Cv extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidateId;

    @Column(name = "file_path", columnDefinition = "NVARCHAR(1000)")
    private String filePath;

    @Column(name = "file_type", columnDefinition = "NVARCHAR(50)")
    private String fileType;

}