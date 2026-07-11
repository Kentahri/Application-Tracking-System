package ats.dto.chat;

import ats.constant.JobStatus;
import ats.entity.Job;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobSuggestionResponse(
        Long id,
        String title,
        String location,
        String department,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        LocalDate deadline,
        JobStatus status
) {

    public static JobSuggestionResponse from(Job job) {
        return new JobSuggestionResponse(
                job.getId(),
                job.getTitle(),
                job.getLocation(),
                job.getDepartmentId() != null ? job.getDepartmentId().getDepartmentName() : null,
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getDeadline(),
                job.getStatus()
        );
    }
}
