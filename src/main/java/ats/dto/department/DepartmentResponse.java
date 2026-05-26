package ats.dto.department;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentResponse {
    private Long id;
    private Long parentId;
    private String departmentName;
    private String description;
}
