package ats.mapper;

import ats.dto.kanban.KanbanApplicationResponse;
import ats.dto.kanban.KanbanBoardResponse;
import ats.dto.kanban.KanbanStageResponse;
import ats.entity.Application;
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
    KanbanApplicationResponse toApplicationResponse(Application application);

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
