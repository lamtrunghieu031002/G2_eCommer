package com.project.ecommerce.services.product;

import com.project.ecommerce.dtos.product.ProductDTO;
import com.project.ecommerce.dtos.product.ProductImageDTO;
import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductImage;
import com.project.ecommerce.responses.ProductResponse;
import com.project.ecommerce.responses.ProductStatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO);
    Product getProductById(long id);
    Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest);
    Product updateProduct(long id, ProductDTO productDTO) ;
    void deleteProduct(long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) ;

    //ProductVariant createVariant(Long productId, ProductVariantDTO variantDTO) throws Exception;

    List<Product> findProductsByIds(List<Long> productIds);

    Page<ProductResponse> findByCategoryId(Long categoryId, Long productId, PageRequest pageRequest);

    Page<ProductStatResponse> getProductStat(LocalDate start, LocalDate end, PageRequest pageRequest);
}
