package com.project.productservice.services.product;

import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.models.ProductImage;

public interface IProductImageService {
    public ProductImage deleteProductImage(Long productId) throws DataNotFoundException;
}
