package ats.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KanbanApplicationResponse {

    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Long cvId;
    private String cvFilePath;
    private String cvFileType;
    private LocalDateTime appliedAt;
}
