package com.project.ecommerce.services.variant;

import com.project.ecommerce.dtos.product.ProductVariantDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductVariant;
import com.project.ecommerce.repositories.ProductRepository;
import com.project.ecommerce.repositories.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VariantService implements IVariantService {

    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    @Override
    public ProductVariant createVariant(Long productId, ProductVariantDTO variantDTO) throws Exception {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Cannot find product with id = " + productId));
        ProductVariant variant = ProductVariant.builder()
                .variant(variantDTO.getVariant())
                .stock(variantDTO.getStock())
                .product(existingProduct)
                .build();

        return variantRepository.save(variant);
    }

    @Override
    public void deleteVariant(Long variantId) {
        Optional<ProductVariant> optionalProductVariant = variantRepository.findById(variantId);
        optionalProductVariant.ifPresent(variantRepository::delete);
    }

    @Override
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return variantRepository.getProductVariantByProductId(productId);
    }

    @Override
    public List<ProductVariant> getVariantsByIds(List<Long> ids) {
        return variantRepository.findVariantsByIds(ids);
    }
}
