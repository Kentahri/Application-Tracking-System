package ats.dto.candidate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CandidateRequest {

    @NotBlank(message = "validation.candidate.name.required")
    @Size(max = 255, message = "validation.candidate.name.size")
    private String name;

    @NotBlank(message = "validation.candidate.email.required")
    @Email(message = "validation.candidate.email.invalid")
    @Size(max = 255, message = "validation.candidate.email.size")
    private String email;

    @NotBlank(message = "validation.candidate.password.required")
    @Size(min = 6, max = 255, message = "validation.candidate.password.size")
    private String password;

    @NotBlank(message = "validation.candidate.phone.required")
    @Size(max = 30, message = "validation.candidate.phone.size")
    @Pattern(regexp = "^[0-9+()\\-\\s]{6,30}$", message = "validation.candidate.phone.pattern")
    private String phone;

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }
}
