package com.foodie.restaurant_service.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "restaurants")
public class Restaurant {
    @Id
    private String restaurantId;
    private String restaurantName;
    private String address;
    private String cuisineType;
    private double rating;
    private List<MenuItem> menu;
}

