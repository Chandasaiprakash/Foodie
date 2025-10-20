package com.foodie.auth_service.controller;

import com.foodie.auth_service.dto.*;
import feign.FeignException;
import com.foodie.auth_service.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.foodie.auth_service.client.UserClient;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    //private final UserRepository userRepository;
    private final UserClient userClient;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        UserAuthDetails createdUser;
        try {
            // 1. Delegate user creation to the User Service
            createdUser = userClient.createUser(req);
        } catch (FeignException.Conflict e) {
            // This happens if the user-service returns a 409 Conflict status
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }

        // 2. Generate a JWT for the newly registered user
        String token = jwtService.generateToken(
                createdUser.id(),
                createdUser.email(),
                createdUser.role(),
                createdUser.username(),
                createdUser.phoneNumber(),
                1000L * 60 * 60 * 24 // 24 hours
        );

        // 3. Return the token and user details
        return new AuthResponse(
                token,
                createdUser.id(),
                createdUser.email(),
                createdUser.role(),
                createdUser.username(),
                createdUser.phoneNumber()
        );
    }


    @Value("${user-service.internal-secret}") // Inject the secret
    private String internalSecret;

    // NOTE on /register: For a full solution, /register should also call the
    // User Service to create the user, but for now we focus on /login validation.
    // If you remove UserRepository, /register will need a Feign POST call to User Service.

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {

        // 1. CALL USER SERVICE via Feign Client
        UserAuthDetails userDetails;
        try {
            userDetails = userClient.getUserByEmailForAuth(req.getEmail(), internalSecret);
        } catch (Exception e) {
            // Feign converts 404 from User Service to a specific error.
            // Map any failure to a generic "Invalid credentials" to prevent enumeration attacks.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 2. VALIDATE PASSWORD LOCALLY
        if (!passwordEncoder.matches(req.getPassword(), userDetails.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // 3. GENERATE TOKEN using data from the User Service
        String token = jwtService.generateToken(
                userDetails.id(),
                userDetails.email(),
                userDetails.role(),
                userDetails.username(),
                userDetails.phoneNumber(),
                1000L * 60 * 60 * 24 // 24 hours
        );

        return new AuthResponse(token, userDetails.id(), userDetails.email(), userDetails.role(), userDetails.username(), userDetails.phoneNumber());
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
                claims.get("username", String.class),
                claims.get("phoneNumber", String.class)
        );
    }

    public record MeResponse(Long id, String email, String role, String username, String phoneNumber) {}
}