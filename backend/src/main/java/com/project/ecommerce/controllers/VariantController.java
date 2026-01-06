package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.product.ProductVariantDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.ProductVariant;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.responses.VariantResponse;
import com.project.ecommerce.services.variant.IVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/variants")
@RequiredArgsConstructor
public class VariantController {

    private final IVariantService variantService;
    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> createVariant(
            @PathVariable("id") Long productId,
            @RequestBody ProductVariantDTO variantDTO) {

        try {
            ProductVariant variant = variantService.createVariant(productId, variantDTO);
            //productVariantRepository.save(variant);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .data(variant)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.CONFLICT)
                    .message(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> deleteVariant(@PathVariable("id") Long variantId) throws DataNotFoundException {
        variantService.deleteVariant(variantId);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(null)
                .message(String.format("Variant with id = %d deleted successfully", variantId))
                .status(HttpStatus.OK)
                .build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getVariantsByProductId(@PathVariable Long productId) {

        try {
            List<ProductVariant> variants = variantService.getVariantsByProductId(productId);
            return ResponseEntity.ok().body(variants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-ids")
    public ResponseEntity<ResponseObject> getVariantsByIds(@RequestParam("ids") String ids){
        List<Long> variantIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        List<ProductVariant> variants = variantService.getVariantsByIds(variantIds);

        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Get variants successfully")
                .data(variants.stream().map(VariantResponse::fromVariant))
                .build());
    }
}
