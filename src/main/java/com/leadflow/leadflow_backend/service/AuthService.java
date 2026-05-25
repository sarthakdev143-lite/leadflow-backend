package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Role;
import com.leadflow.leadflow_backend.dto.LoginRequest;
import com.leadflow.leadflow_backend.dto.LoginResponse;
import com.leadflow.leadflow_backend.dto.RegisterRequest;
import com.leadflow.leadflow_backend.exception.AuthException;
import com.leadflow.leadflow_backend.model.User;
import com.leadflow.leadflow_backend.repos.UserRepository;
import com.leadflow.leadflow_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.Map;

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
    public Map<String, String> register(
            RegisterRequest request
    ) {

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException(
                    "Passwords do not match"
            );
        }

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }

        if (userRepository.existsByPhone(
                request.getPhone())) {

            throw new RuntimeException(
                    "Phone number already registered"
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .phone(request.getPhone())
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        log.info(
                "New admin registered successfully: {}",
                user.getEmail()
        );

        return Map.of(
                "message",
                "Account created successfully"
        );
    }

}