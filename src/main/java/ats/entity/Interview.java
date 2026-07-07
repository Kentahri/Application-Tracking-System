package ats.entity;

import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "interviews")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE interviews SET is_deleted = 1 WHERE id = ? and is_deleted = 0")
@SQLRestriction("is_deleted = 0")
public class Interview extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application applicationId;

    @ManyToOne
    @JoinColumn(name = "interviewer_id")
    private User interviewerId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "meeting_link", columnDefinition = "NVARCHAR(1000)")
    private String meetingLink;

    @Column(columnDefinition = "NVARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    @Column(name  = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "feed_back", columnDefinition = "NVARCHAR(2000)")
    private String feedBack;

    @Column(columnDefinition = "NVARCHAR(50)")
    @Enumerated(EnumType.STRING)
    private InterviewResult result;

}

