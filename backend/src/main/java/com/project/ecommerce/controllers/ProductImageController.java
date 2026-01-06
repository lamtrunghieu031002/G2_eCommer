package com.project.ecommerce.controllers;

import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.ProductImage;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.product.IProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("${api.prefix}/product_images")
@RequiredArgsConstructor
public class ProductImageController {

    private final IProductImageService productImageService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> delete(@PathVariable Long id) throws DataNotFoundException, IOException {
        ProductImage productImage = productImageService.deleteProductImage(id);
        if( productImage != null) {
            Path uploadDir = Paths.get("uploads");
            Path filePath = uploadDir.resolve(productImage.getImageUrl());
            if(Files.exists(filePath)){
                Files.delete(filePath);
            } else {
                throw new FileNotFoundException(String.format("Cannot find file with url: %s", filePath));
            }
        }
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Delete product image successfully")
                        .data(productImage)
                        .status(HttpStatus.OK)
                        .build());
    }
}
