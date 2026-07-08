package com.project.productservice.services.product;

import com.project.productservice.dtos.product.ProductDTO;
import com.project.productservice.dtos.product.ProductImageDTO;
import com.project.productservice.models.Product;
import com.project.productservice.models.ProductImage;
import com.project.productservice.responses.ProductResponse;
import com.project.productservice.responses.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
    Product createProduct(ProductDTO productDTO);
    Product getProductById(long id);
    Page<ProductResponse> getAllProducts(String keyword,
                                                Long categoryId, PageRequest pageRequest);
    ProductListResponse getProductsCached(String keyword, Long categoryId, int page, int limit);
    Product updateProduct(long id, ProductDTO productDTO) ;
    void deleteProduct(long id);
    boolean existsByName(String name);
    ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) ;

    //ProductVariant createVariant(Long productId, ProductVariantDTO variantDTO) throws Exception;

    List<Product> findProductsByIds(List<Long> productIds);

    Page<ProductResponse> findByCategoryId(Long categoryId, Long productId, PageRequest pageRequest);
}
