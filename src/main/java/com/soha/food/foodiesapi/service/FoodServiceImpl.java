package com.soha.food.foodiesapi.service;

import com.soha.food.foodiesapi.entity.FoodEntity;
import com.soha.food.foodiesapi.repository.FoodRepository;
import com.soha.food.foodiesapi.request.FoodRequest;
import com.soha.food.foodiesapi.request.FoodResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {

    private final S3Client s3Client;
    private final FoodRepository foodRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {

        log.info("Starting file upload. Original filename: {}", file.getOriginalFilename());

        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));

        String key = UUID.randomUUID() + extension;

        log.debug("Generated S3 key: {}", key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            log.info("Uploading file to S3. Bucket: {}, Key: {}", bucketName, key);

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

            log.info("File uploaded successfully. URL: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to S3. Filename: {}", file.getOriginalFilename(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload file",
                    e
            );
        }
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {

        log.info("Adding new food. Name: {}, Category: {}", request.getName(), request.getCategory());

        FoodEntity newFoodEntity = convertTOEntity(request);

        log.debug("Food entity created (before save): {}", newFoodEntity);

        String foodImageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(foodImageUrl);

        log.info("Saving food entity to MongoDB");

        newFoodEntity = foodRepository.save(newFoodEntity);

        log.info("Food saved successfully. ID: {}", newFoodEntity.getId());

        return convertToResponse(newFoodEntity);
    }

    @Override
    public List<FoodResponse> readFoods() {
        return foodRepository.findAll().stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(String id) {
        FoodEntity food =  foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found for id" + id));
        return convertToResponse(food);
    }

    @Override
    public Boolean deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        return true;
    }

    @Override
    public void deleteFood(String id) {
        String fileName = readFood(id).getImageUrl();
        fileName = fileName.replace("https://" + bucketName + ".s3.amazonaws.com/", "");
        if(deleteFile(fileName)) {
            foodRepository.deleteById(id);
        }
    }

    private FoodEntity convertTOEntity(FoodRequest request) {

        log.debug("Converting FoodRequest to FoodEntity");

        return FoodEntity.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
    }

    private FoodResponse convertToResponse(FoodEntity entity) {

        log.debug("Converting FoodEntity to FoodResponse. ID: {}", entity.getId());

        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .imageUrl(entity.getImageUrl())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .build();
    }
}
