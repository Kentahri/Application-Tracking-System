package ats.mapper;

import ats.dto.application.ApplicationDetailResponse;
import ats.dto.application.MoveApplicationStageResponse;
import ats.entity.Application;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.entity.Interview;
import ats.entity.Job;
import ats.entity.PipelineStage;
import ats.entity.StageTransition;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    default MoveApplicationStageResponse toMoveStageResponse(Application application,
                                                             PipelineStage fromStage,
                                                             PipelineStage toStage,
                                                             LocalDateTime movedAt) {
        return new MoveApplicationStageResponse(
                application.getId(),
                fromStage != null ? fromStage.getId() : null,
                fromStage != null ? fromStage.getStageName() : null,
                toStage.getId(),
                toStage.getStageName(),
                movedAt
        );
    }

    default ApplicationDetailResponse toDetailResponse(Application application,
                                                       List<StageTransition> stageTransitions,
                                                       Interview interview) {
        return new ApplicationDetailResponse(
                application.getId(),
                application.getPriority(),
                application.getCreatedAt(),
                toJobInfo(application.getJobId()),
                toCandidateInfo(application.getCandidateId()),
                toCvInfo(application.getCvId()),
                toStageInfo(application.getPipelineStageId()),
                stageTransitions.stream()
                        .map(this::toStageHistoryInfo)
                        .toList(),
                toInterviewInfo(interview)
        );
    }

    default ApplicationDetailResponse.JobInfo toJobInfo(Job job) {
        if (job == null) {
            return null;
        }
        return new ApplicationDetailResponse.JobInfo(
                job.getId(),
                job.getTitle(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getStatus()
        );
    }

    default ApplicationDetailResponse.CandidateInfo toCandidateInfo(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        return new ApplicationDetailResponse.CandidateInfo(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getPhone()
        );
    }

    default ApplicationDetailResponse.CvInfo toCvInfo(Cv cv) {
        if (cv == null) {
            return null;
        }
        return new ApplicationDetailResponse.CvInfo(
                cv.getId(),
                cv.getFilePath(),
                cv.getFileType()
        );
    }

    default ApplicationDetailResponse.StageInfo toStageInfo(PipelineStage stage) {
        if (stage == null) {
            return null;
        }
        return new ApplicationDetailResponse.StageInfo(
                stage.getId(),
                stage.getStageName(),
                stage.getStageOrder()
        );
    }

    default ApplicationDetailResponse.StageHistoryInfo toStageHistoryInfo(StageTransition transition) {
        return new ApplicationDetailResponse.StageHistoryInfo(
                transition.getId(),
                toStageInfo(transition.getFromStageId()),
                toStageInfo(transition.getToStageId()),
                transition.getNotes(),
                transition.getMovedAt()
        );
    }

    default ApplicationDetailResponse.InterviewInfo toInterviewInfo(Interview interview) {
        if (interview == null) {
            return null;
        }
        return new ApplicationDetailResponse.InterviewInfo(
                interview.getId(),
                interview.getInterviewerId() != null ? interview.getInterviewerId().getId() : null,
                interview.getInterviewerId() != null ? interview.getInterviewerId().getName() : null,
                interview.getScheduledAt(),
                interview.getDurationMinutes(),
                interview.getMeetingLink(),
                interview.getStatus(),
                interview.getResult(),
                interview.getFeedBack()
        );
    }
}
