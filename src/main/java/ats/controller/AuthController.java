package ats.controller;

import ats.dto.auth.LoginRequest;
import ats.service.impl.CustomUserDetailsService;
import ats.service.impl.JwtService;
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
public class AuthController {

    private AuthenticationManager authManager;

    private JwtService jwtService;

    private CustomUserDetailsService userService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {

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
