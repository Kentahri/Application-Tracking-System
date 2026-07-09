package ats.dto.user;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UserRequest {

    private Long departmentId;

    @NotBlank(message = "validation.user.name.required")
    @Size(max = 255, message = "validation.user.name.size")
    private String name;

    @NotBlank(message = "validation.user.email.required")
    @Email(message = "validation.user.email.invalid")
    @Size(max = 255, message = "validation.user.email.size")
    private String email;

    @NotBlank(message = "validation.user.password.required")
    @Size(min = 6, max = 255, message = "validation.user.password.size")
    private String password;

    @NotBlank(message = "validation.user.phone.required")
    @Size(max = 30, message = "validation.user.phone.size")
    @Pattern(regexp = "^[0-9+()\\-\\s]{6,30}$", message = "validation.user.phone.pattern")
    private String phone;

    @NotNull(message = "validation.user.role.required")
    private UserRole role;

    @NotNull(message = "validation.user.status.required")
    private UserStatus status;

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
