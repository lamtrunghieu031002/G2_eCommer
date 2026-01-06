package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.payment.PaymentDTO;
import com.project.ecommerce.dtos.payment.PaymentQueryDTO;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.payment.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payments")
public class PaymentController {

    private final IVNPayService vnPayService;

    @PostMapping("/create_payment_url")
    public ResponseEntity<ResponseObject> createPayment(@RequestBody PaymentDTO paymentDTO, HttpServletRequest request) {
        String paymentUrl = vnPayService.createPaymentUrl(paymentDTO, request);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Payment URL generated successfully.")
                .data(paymentUrl)
                .build());
    }

    @PostMapping("/query")
    public ResponseEntity<ResponseObject> queryTransaction(@RequestBody PaymentQueryDTO queryDTO, HttpServletRequest request) throws IOException {
        String queryUrl = vnPayService.queryTransaction(queryDTO, request);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Successfully")
                .data(queryUrl)
                .build());
    }
}
