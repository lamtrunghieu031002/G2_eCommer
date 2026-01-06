package com.project.ecommerce.services.product;

import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.ProductImage;

public interface IProductImageService {
    public ProductImage deleteProductImage(Long productId) throws DataNotFoundException;
}
