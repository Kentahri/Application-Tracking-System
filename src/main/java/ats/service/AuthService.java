package ats.service;

import ats.dto.auth.LoginRequest;
import ats.dto.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse candidateLogin(LoginRequest request);
}
