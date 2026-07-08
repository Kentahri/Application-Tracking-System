package ats.mapper;

import ats.dto.kanban.KanbanApplicationResponse;
import ats.dto.kanban.KanbanBoardResponse;
import ats.dto.kanban.KanbanStageResponse;
import ats.entity.Application;
import ats.entity.Interview;
import ats.entity.Job;
import ats.entity.PipelineStage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KanbanMapper {

    @Mapping(target = "applicationId", source = "id")
    @Mapping(target = "candidateId", source = "candidateId.id")
    @Mapping(target = "candidateName", source = "candidateId.name")
    @Mapping(target = "candidateEmail", source = "candidateId.email")
    @Mapping(target = "candidatePhone", source = "candidateId.phone")
    @Mapping(target = "cvId", source = "cvId.id")
    @Mapping(target = "cvFilePath", source = "cvId.filePath")
    @Mapping(target = "cvFileType", source = "cvId.fileType")
    @Mapping(target = "appliedAt", source = "createdAt")
    @Mapping(target = "interviewId", ignore = true)
    @Mapping(target = "interviewerId", ignore = true)
    @Mapping(target = "interviewerName", ignore = true)
    @Mapping(target = "interviewScheduledAt", ignore = true)
    @Mapping(target = "interviewStatus", ignore = true)
    @Mapping(target = "interviewResult", ignore = true)
    KanbanApplicationResponse toApplicationResponse(Application application);

    default KanbanApplicationResponse toApplicationResponse(Application application, Interview interview) {
        KanbanApplicationResponse response = toApplicationResponse(application);
        if (interview == null) {
            return response;
        }

        response.setInterviewId(interview.getId());
        response.setInterviewerId(interview.getInterviewerId() != null ? interview.getInterviewerId().getId() : null);
        response.setInterviewerName(interview.getInterviewerId() != null ? interview.getInterviewerId().getName() : null);
        response.setInterviewScheduledAt(interview.getScheduledAt());
        response.setInterviewStatus(interview.getStatus());
        response.setInterviewResult(interview.getResult());
        return response;
    }

    default KanbanStageResponse toStageResponse(PipelineStage stage,
                                                List<KanbanApplicationResponse> applications) {
        return new KanbanStageResponse(
                stage.getId(),
                stage.getStageName(),
                stage.getStageOrder(),
                (long) applications.size(),
                applications
        );
    }

    default KanbanBoardResponse toBoardResponse(Job job, List<KanbanStageResponse> stages, long totalApplications) {
        return new KanbanBoardResponse(
                job.getId(),
                job.getTitle(),
                totalApplications,
                stages
        );
    }
}
