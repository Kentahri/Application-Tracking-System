package quan.ats.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepartmentRequest {

    private Long parentId;

    @Size(max = 255, message = "validation.department.name.size")
    @NotBlank(message = "validation.department.name.null")
    private String departmentName;

    private String description;
}
