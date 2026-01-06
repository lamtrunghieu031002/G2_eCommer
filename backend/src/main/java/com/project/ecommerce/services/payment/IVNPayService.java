package com.project.ecommerce.services.payment;

import com.project.ecommerce.dtos.payment.PaymentDTO;
import com.project.ecommerce.dtos.payment.PaymentQueryDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface IVNPayService {

    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
//    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
