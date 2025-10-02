package com.foodie.user_service.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String phone;
    private String role; // CUSTOMER, DELIVERY_PARTNER, ADMIN

    @ElementCollection
    private List<Address> addresses;
}

