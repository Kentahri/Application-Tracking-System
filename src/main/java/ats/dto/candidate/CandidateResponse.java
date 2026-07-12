package ats.dto.candidate;

import ats.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CandidateResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer numberOfQueryQuota;
    private UserStatus candidateStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
