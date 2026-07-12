package ats.dto.candidate;

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
public class CandidateChangePasswordRequest {

    @NotBlank(message = "validation.candidate.oldPassword.required")
    @Size(min = 6, max = 255, message = "validation.candidate.oldPassword.size")
    private String oldPassword;

    @NotBlank(message = "validation.candidate.newPassword.required")
    @Size(min = 6, max = 255, message = "validation.candidate.newPassword.size")
    private String newPassword;

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword == null ? null : oldPassword.trim();
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword == null ? null : newPassword.trim();
    }
}
