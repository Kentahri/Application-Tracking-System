package ats.dto.auth;

import ats.constant.UserRole;
import ats.entity.Candidate;
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

    public static LoginResponse fromCandidate(Candidate candidate, String accessToken) {
        return new LoginResponse(
                "Login successful",
                accessToken,
                UserInfo.from(candidate)
        );
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfo {

        private Long id;

        private String username;

        private String fullName;

        private UserRole role;

        private Integer numberOfQueryQuota;

        private String membershipName;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    null,
                    null
            );
        }

        public static UserInfo from(Candidate candidate) {
            String membershipName = candidate.getUpgradePackageId() != null
                    ? candidate.getUpgradePackageId().getPackageName()
                    : null;

            return new UserInfo(
                    candidate.getId(),
                    candidate.getEmail(),
                    candidate.getName(),
                    UserRole.CANDIDATE,
                    candidate.getNumberOfQueryQuota(),
                    membershipName
            );
        }
    }
}
