// Auth Service: com.foodie.auth_service.client.UserClient
package com.foodie.auth_service.client;

import com.foodie.auth_service.dto.RegisterRequest;
import com.foodie.auth_service.dto.UserAuthDetails; // You need this DTO in Auth Service
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserClient {

    // Matches the User Service internal endpoint
    @GetMapping("/internal/users/by-email/{email}")
    UserAuthDetails getUserByEmailForAuth(@PathVariable("email") String email,
                                          @RequestHeader("X-Internal-Secret") String secret);

    @PostMapping("/users")
    UserAuthDetails createUser(@RequestBody RegisterRequest registerRequest);
}