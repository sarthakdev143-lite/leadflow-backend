package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.dto.LoginRequest;
import com.leadflow.leadflow_backend.dto.LoginResponse;
import com.leadflow.leadflow_backend.exception.AuthException;
import com.leadflow.leadflow_backend.model.User;
import com.leadflow.leadflow_backend.repos.UserRepository;
import com.leadflow.leadflow_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {

        log.info(
                "Login attempt for email: {}",
                request.getEmail()
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {

                    log.error(
                            "User not found: {}",
                            request.getEmail()
                    );

                    return new AuthException(
                            "Invalid email or password"
                    );
                });

        boolean passwordMatched =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatched) {

            log.error(
                    "Invalid password for email: {}",
                    request.getEmail()
            );

            throw new AuthException(
                    "Invalid email or password"
            );
        }

        String token =
                jwtUtil.generateToken(user.getEmail());

        log.info(
                "Login successful for email: {}",
                request.getEmail()
        );

        return new LoginResponse(
                token,
                user.getRole().name()
        );
    }
}