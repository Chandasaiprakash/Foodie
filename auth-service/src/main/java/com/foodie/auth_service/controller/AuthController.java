package com.foodie.auth_service.controller;

import com.foodie.auth_service.dto.*;
import com.foodie.auth_service.model.User;
import com.foodie.auth_service.repository.UserRepository;
import com.foodie.auth_service.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String role = (req.getRole() == null) ? "CUSTOMER" : req.getRole().toUpperCase();
        User u = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();
        userRepository.save(u);

        // ✅ Fixed token generation - create proper claims
        String token = jwtService.generateToken(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getUsername(),
                1000L * 60 * 60 * 24 // 24 hours
        );

        return new AuthResponse(token, u.getId(), u.getEmail(), u.getRole(), u.getUsername());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // ✅ Fixed token generation
        String token = jwtService.generateToken(
                u.getId(),
                u.getEmail(),
                u.getRole(),
                u.getUsername(),
                1000L * 60 * 60 * 24 // 24 hours
        );

        return new AuthResponse(token, u.getId(), u.getEmail(), u.getRole(), u.getUsername());
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "

        // Validate token first
        if (!jwtService.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        Claims claims = jwtService.getClaims(token);

        return new MeResponse(
                Long.parseLong(claims.getSubject()), // Id
                claims.get("email", String.class),
                claims.get("role", String.class),
                claims.get("username", String.class)
        );
    }

    public record MeResponse(Long id, String email, String role, String username) {}
}