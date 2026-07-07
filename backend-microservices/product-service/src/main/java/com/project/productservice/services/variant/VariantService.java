package com.project.productservice.services.variant;

import com.project.productservice.dtos.product.ProductVariantDTO;
import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.models.Product;
import com.project.productservice.models.ProductVariant;
import com.project.productservice.repositories.ProductRepository;
import com.project.productservice.repositories.VariantRepository;
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
