package ats.controller;

import ats.service.impl.CustomUserDetailsService;
import ats.service.impl.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@AllArgsConstructor
public class DevAuthController {

    private final CustomUserDetailsService userService;
    private final JwtService jwtService;

    @GetMapping("/token")
    public String token(@RequestParam String email) {
        UserDetails user = userService.loadUserByUsername(email);
        return jwtService.generateToken(user);
    }
}
