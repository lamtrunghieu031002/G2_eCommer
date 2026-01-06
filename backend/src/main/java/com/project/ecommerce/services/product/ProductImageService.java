package com.project.ecommerce.services.product;

import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.ProductImage;
import com.project.ecommerce.repositories.ProductImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService{
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public ProductImage deleteProductImage(Long imageId)
            throws DataNotFoundException {
        productImageRepository.deleteById(imageId);
        return null;
    }
}
