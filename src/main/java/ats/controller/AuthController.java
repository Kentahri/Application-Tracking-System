package ats.controller;

import ats.dto.auth.LoginRequest;
import ats.dto.auth.LoginResponse;
import ats.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user credentials and return a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/candidate/login")
    @Operation(summary = "Candidate login", description = "Authenticate candidate credentials and return a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public LoginResponse candidateLogin(@Valid @RequestBody LoginRequest req) {
        return authService.candidateLogin(req);
    }
}
