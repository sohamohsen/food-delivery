package com.soha.food.foodiesapi.controller;

import com.soha.food.foodiesapi.request.FoodRequest;
import com.soha.food.foodiesapi.request.FoodResponse;
import com.soha.food.foodiesapi.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    @PostMapping(
            value = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public FoodResponse addFood(
            @RequestPart("foods") String foodString,
            @RequestPart("file") MultipartFile file
    ) {
        ObjectMapper mapper = new ObjectMapper();
        FoodRequest request = mapper.readValue(foodString, FoodRequest.class);
        System.out.println(request);
        return foodService.addFood(request, file);
    }

    @GetMapping("/all")
    public List<FoodResponse> getFoods(){
        return foodService.readFoods();
    }

    @GetMapping("/food/{id}")
    public FoodResponse getFood(@PathVariable String id) {
        try {
            return foodService.readFood(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
        }
    }

    @DeleteMapping("/remove-food/{id}")
    public void deleteFood(@PathVariable String id) {
        try {
            foodService.deleteFood(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
        }    }

}