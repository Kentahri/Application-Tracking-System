package ats.mapper;

import ats.dto.candidate.CandidateRequest;
import ats.dto.candidate.CandidateResponse;
import ats.entity.Candidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateResponse toDto(Candidate candidate);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "candidateStatus", ignore = true)
    @Mapping(target = "numberOfQueryQuota", ignore = true)
    @Mapping(target = "upgradePackageId", ignore = true)
    @Mapping(target = "payments", ignore = true)
    Candidate toEntity(CandidateRequest request);
}
