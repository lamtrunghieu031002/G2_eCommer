package com.project.productservice.services.variant;

import com.project.productservice.dtos.product.ProductVariantDTO;
import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.models.ProductVariant;

import java.util.List;

public interface IVariantService {

    ProductVariant createVariant(Long productId, ProductVariantDTO variantDTO) throws Exception;
    void deleteVariant(Long variantId) throws DataNotFoundException;

    List<ProductVariant> getVariantsByProductId(Long productId);

    List<ProductVariant> getVariantsByIds(List<Long> ids);
}
