package com.foodie.restaurant_service.controller;


import com.foodie.restaurant_service.model.Restaurant;
import com.foodie.restaurant_service.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<Restaurant> getAll() {
        return restaurantService.getAll();
    }

    @GetMapping("/{restaurantId}")
    public Restaurant getByRestaurantId(@PathVariable String restaurantId) {
        return restaurantService.getByRestaurantId(restaurantId);
    }

    @PostMapping
    public Restaurant add(@RequestBody Restaurant restaurant) {
        return restaurantService.save(restaurant);
    }

    @DeleteMapping("/{restaurantId}")
    public void delete(@PathVariable String restaurantId) {
        restaurantService.delete(restaurantId);
    }

    @GetMapping("/search")
    public List<Restaurant> search(@RequestParam String restaurantName) {
        return restaurantService.searchByRestaurantName(restaurantName);
    }

    @GetMapping("/filter")
    public List<Restaurant> filter(@RequestParam String cuisine) {
        return restaurantService.filterByCuisine(cuisine);
    }
}

