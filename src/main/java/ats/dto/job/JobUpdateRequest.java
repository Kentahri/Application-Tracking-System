package ats.dto.job;

import ats.constant.JobStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobUpdateRequest {

    private Long departmentId;

    @Size(max = 500, message = "{validation.job.title.size}")
    private String title;

    private String description;

    @Size(max = 500, message = "{validation.job.location.size}")
    private String location;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private JobStatus status;
}

