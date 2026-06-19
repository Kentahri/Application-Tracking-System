package ats.dto.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class JobRequest {

    @NotNull(message = "{validation.job.departmentId.null}")
    private Long departmentId;

    @Size(max = 500, message = "{validation.job.title.size}")
    @NotBlank(message = "{validation.job.title.null}")
    private String title;

    private String description;

    @Size(max = 500, message = "{validation.job.location.size}")
    private String location;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    @Size(max = 50, message = "{validation.job.status.size}")
    private String status;
}

