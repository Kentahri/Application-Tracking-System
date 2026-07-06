package ats.service.impl;

import ats.dto.auth.LoginRequest;
import ats.dto.auth.LoginResponse;
import ats.entity.User;
import ats.exception.UnauthorizedException;
import ats.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userService;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getHashPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password", ex);
        }

        CustomUserDetails userDetails = (CustomUserDetails) userService.loadUserByUsername(request.getEmail());
        User user = userDetails.getUser();
        String accessToken = jwtService.generateToken(userDetails);

        return LoginResponse.from(user, accessToken);
    }
}
