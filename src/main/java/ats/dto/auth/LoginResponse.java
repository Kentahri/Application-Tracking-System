package ats.dto.auth;

import ats.constant.UserRole;
import ats.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String message;

    private String accessToken;

    private UserInfo user;

    public static LoginResponse from(User user, String accessToken) {
        return new LoginResponse(
                "Login successful",
                accessToken,
                UserInfo.from(user)
        );
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfo {

        private Long id;

        private String username;

        private String fullName;

        private UserRole role;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole()
            );
        }
    }
}
