package com.soha.food.foodiesapi.service;

import com.soha.food.foodiesapi.request.FoodRequest;
import com.soha.food.foodiesapi.request.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {

    String uploadFile(MultipartFile file);

    FoodResponse addFood(FoodRequest request, MultipartFile file);

    List<FoodResponse> readFoods();

    FoodResponse readFood(String id);

    Boolean deleteFile(String fileName);

    void deleteFood(String id);
}
