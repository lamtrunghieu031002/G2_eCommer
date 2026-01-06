package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.product.ProductDTO;
import com.project.ecommerce.dtos.product.ProductImageDTO;
import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductImage;
import com.project.ecommerce.responses.ProductListResponse;
import com.project.ecommerce.responses.ProductResponse;
import com.project.ecommerce.responses.ProductStatResponse;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.product.IProductService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createProduct(
            @Valid @RequestBody ProductDTO productDTO
    ) {
        Product newProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Create product successfully!")
                .data(newProduct)
                .build());
    }

    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Product existingProduct = productService.getProductById(productId);
            files = files == null ? new ArrayList<>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .message(MessageKeys.UPLOAD_IMAGES_MAX_5)
                        .build());
            }
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if(file.getSize() == 0) {
                    continue;
                }
                // Kiểm tra kích thước file và định dạng
                if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body(ResponseObject.builder()
                                    .message(MessageKeys.UPLOAD_IMAGES_FILE_LARGE)
                                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                    .build());
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body(ResponseObject.builder()
                                    .message(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE)
                                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                    .build());
                }
                // Lưu file và cập nhật thumbnail trong DTO
                String filename = storeFile(file); // Thay thế hàm này với code của bạn để lưu file
                //lưu vào đối tượng product trong DB
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Upload image successfully")
                    .status(HttpStatus.CREATED)
                    .data(productImages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.CONFLICT)
                    .data(e.getMessage())
                    .build());
        }
    }

//    @PostMapping("/variants/{productId}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    private ResponseEntity<?> createVariant(
//            @PathVariable Long productId,
//            @RequestBody ProductVariantDTO variantDTO) {
//
//        try {
//            ProductVariant variant = productService.createVariant(productId, variantDTO);
//            //productVariantRepository.save(variant);
//            return ResponseEntity.ok().body(variant);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
            UrlResource resource = new UrlResource(imagePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()));

            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    private String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
        // Kiểm tra và tạo thư mục nếu nó không tồn tại
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Đường dẫn đầy đủ đến file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // Sao chép file vào thư mục đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    @GetMapping("")
    public ResponseEntity<ResponseObject> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        int totalPages = 0;
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit
                //Sort.by("createdAt").descending()

        );
        Page<ProductResponse> productResponses =  productService
                .getAllProducts(keyword, categoryId, pageRequest);

        totalPages = productResponses.getTotalPages();
        ProductListResponse productListResponse = ProductListResponse
                .builder()
                .products(productResponses.stream().toList())
                .totalPages(totalPages)
                .build();

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get products successfully")
                .status(HttpStatus.OK)
                .data(productListResponse)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getProductById(
            @PathVariable("id") Long productId
    ) {

        Product existingProduct = productService.getProductById(productId);
        ProductResponse productResponse = ProductResponse.fromProduct(existingProduct);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(productResponse)
                .build());

    }

    @GetMapping("/{categoryId}/{productId}")
    public ResponseEntity<ResponseObject> getProductByCategoryId(
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("productId") Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        int totalPages = 0;

        Page<ProductResponse> products = productService.findByCategoryId(categoryId, productId, pageRequest);
        totalPages = products.getTotalPages();
        ProductListResponse productListResponse = ProductListResponse.builder()
                .products(products.getContent())
                .totalPages(totalPages)
                .build();
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(productListResponse)
                .message("Success!")
                .build());

    }

    @GetMapping("/by-ids")
    public ResponseEntity<ResponseObject> getProductsByIds(@RequestParam("ids") String ids) {
        //eg: 1,3,5,7
        List<Long> productIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        List<Product> products = productService.findProductsByIds(productIds);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(products.stream().map(ProductResponse::fromProduct).toList())
                .message("Get products successfully")
                .status(HttpStatus.OK)
                .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteProduct(@PathVariable long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(null)
                .message(String.format("Product with id = %d deleted successfully", id))
                .status(HttpStatus.OK)
                .build());
    }
    //@PostMapping("/generateFakeProducts")
//    private ResponseEntity<String> generateFakeProducts() {
//        Faker faker = new Faker();
//        for (int i = 0; i < 1_000_000; i++) {
//            String productName = faker.commerce().productName();
//            if(productService.existsByName(productName)) {
//                continue;
//            }
//            ProductDTO productDTO = ProductDTO.builder()
//                    .name(productName)
//                    .price((float)faker.number().numberBetween(10, 90_000_000))
//                    .description(faker.lorem().sentence())
//                    .thumbnail("")
//                    .categoryId((long)faker.number().numberBetween(2, 5))
//                    .build();
//            try {
//                productService.createProduct(productDTO);
//            } catch (Exception e) {
//                return ResponseEntity.badRequest().body(e.getMessage());
//            }
//        }
//        return ResponseEntity.ok("Fake Products created successfully");
//    }

    //update a product
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> updateProduct(
            @PathVariable long id,
            @Valid @RequestBody ProductDTO productDTO) {
        Product updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(ProductResponse.fromProduct(updatedProduct))
                .message("Update product successfully!")
                .status(HttpStatus.OK)
                .build());
    }
}
