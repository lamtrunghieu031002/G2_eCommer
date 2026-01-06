package com.project.ecommerce.services.variant;

import com.project.ecommerce.dtos.product.ProductVariantDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.ProductVariant;

import java.util.List;

public interface IVariantService {

    ProductVariant createVariant(Long productId, ProductVariantDTO variantDTO) throws Exception;
    void deleteVariant(Long variantId) throws DataNotFoundException;

    List<ProductVariant> getVariantsByProductId(Long productId);

    List<ProductVariant> getVariantsByIds(List<Long> ids);
}
