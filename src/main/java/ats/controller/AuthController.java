package ats.controller;

import ats.dto.auth.LoginRequest;
import ats.service.impl.CustomUserDetailsService;
import ats.service.impl.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private AuthenticationManager authManager;

    private JwtService jwtService;

    private CustomUserDetailsService userService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user credentials and return a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public String login(@Valid @RequestBody LoginRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getHashPassword()
                )
        );

        UserDetails user = userService.loadUserByUsername(req.getEmail());

        return jwtService.generateToken(user);
    }
}
