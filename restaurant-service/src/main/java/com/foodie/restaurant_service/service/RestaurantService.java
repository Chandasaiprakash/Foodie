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

    public Restaurant getById(String id) {
        return restaurantRepository.findById(id).orElseThrow();
    }

    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public void delete(String id) {
        restaurantRepository.deleteById(id);
    }

    public List<Restaurant> searchByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Restaurant> filterByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineType(cuisine);
    }
}

