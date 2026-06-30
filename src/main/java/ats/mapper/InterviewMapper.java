package ats.mapper;

import ats.dto.interview.InterviewScheduleResponse;
import ats.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "applicationId", source = "applicationId.id")
    @Mapping(target = "interviewerId", source = "interviewerId.id")
    @Mapping(target = "interviewerName", source = "interviewerId.name")
    @Mapping(target = "candidateId", source = "applicationId.candidateId.id")
    @Mapping(target = "candidateName", source = "applicationId.candidateId.name")
    @Mapping(target = "candidateEmail", source = "applicationId.candidateId.email")
    @Mapping(target = "candidatePhone", source = "applicationId.candidateId.phone")
    @Mapping(target = "jobId", source = "applicationId.jobId.id")
    @Mapping(target = "jobTitle", source = "applicationId.jobId.title")
    @Mapping(target = "feedback", source = "feedBack")
    InterviewScheduleResponse toScheduleResponse(Interview interview);
}
