package ats.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyUploadRequest {

    @NotBlank(message = "validation.fullName.required")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email.invalid")
    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 2000)
    private String message;

    @JsonIgnore
    private transient MultipartFile cvFile;
}
