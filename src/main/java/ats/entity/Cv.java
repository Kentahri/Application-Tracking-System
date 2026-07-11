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
@Table(name = "cvs")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE cvs SET is_deleted = 1, updated_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Cv extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidateId;

    @Column(name = "file_path", columnDefinition = "NVARCHAR(1000)")
    private String filePath;

    @Column(name = "file_name", columnDefinition = "NVARCHAR(500)")
    private String fileName;

    @Column(name = "file_type", columnDefinition = "NVARCHAR(255)")
    private String fileType;

    @Column(name = "parsed_text", columnDefinition = "NVARCHAR(MAX)")
    private String parsedText;

    @Column(name = "parsed_at")
    private LocalDateTime parsedAt;
}
