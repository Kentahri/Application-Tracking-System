package ats.dto.user;

import ats.constant.UserRole;
import ats.constant.UserStatus;
import jakarta.validation.constraints.Email;
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
public class UserUpdateRequest {

    private Long departmentId;

    @Size(max = 255, message = "validation.user.name.size")
    @Pattern(regexp = "^(?!\\s*$).+", message = "validation.user.name.pattern")
    private String name;

    @Email(message = "validation.user.email.invalid")
    @Size(max = 255, message = "validation.user.email.size")
    @Pattern(regexp = "^(?!\\s*$).+", message = "validation.user.email.pattern")
    private String email;

    @Size(min = 6, max = 255, message = "validation.user.password.size")
    private String password;

    @Size(max = 30, message = "validation.user.phone.size")
    @Pattern(regexp = "^(?!\\s*$).+", message = "validation.user.phone.pattern")
    private String phone;

    private UserRole role;

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
