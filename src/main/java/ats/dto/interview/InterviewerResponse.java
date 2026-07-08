package ats.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InterviewerResponse {

    private Long id;
    private String fullName;
    private String email;
    private Long departmentId;
}
