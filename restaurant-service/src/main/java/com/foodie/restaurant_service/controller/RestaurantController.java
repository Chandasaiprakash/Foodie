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

    @GetMapping("/{id}")
    public Restaurant getById(@PathVariable String id) {
        return restaurantService.getById(id);
    }

    @PostMapping
    public Restaurant add(@RequestBody Restaurant restaurant) {
        return restaurantService.save(restaurant);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        restaurantService.delete(id);
    }

    @GetMapping("/search")
    public List<Restaurant> search(@RequestParam String name) {
        return restaurantService.searchByName(name);
    }

    @GetMapping("/filter")
    public List<Restaurant> filter(@RequestParam String cuisine) {
        return restaurantService.filterByCuisine(cuisine);
    }
}

