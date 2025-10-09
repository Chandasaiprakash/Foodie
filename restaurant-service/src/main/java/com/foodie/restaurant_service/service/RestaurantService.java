package com.foodie.restaurant_service.service;


import com.foodie.restaurant_service.model.Restaurant;
import com.foodie.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> getAll() {
        return restaurantRepository.findAll();
    }

    public Restaurant getByRestaurantId(String restaurantId) {
        return restaurantRepository.findByRestaurantId(restaurantId);
    }

    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public void delete(String restaurantId) {
        restaurantRepository.deleteByRestaurantId(restaurantId);
    }

    public List<Restaurant> searchByRestaurantName(String restaurantName) {
        return restaurantRepository.findByRestaurantNameContainingIgnoreCase(restaurantName);
    }

    public List<Restaurant> filterByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineType(cuisine);
    }
}

