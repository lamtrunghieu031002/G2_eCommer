package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.order.OrderDetailDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.OrderDetail;
import com.project.ecommerce.responses.OrderDetailResponse;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.orderDetail.IOrderDetailService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final IOrderDetailService orderDetailService;
    //Thêm mới 1 order detail
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> createOrderDetail(
            @Valid  @RequestBody OrderDetailDTO orderDetailDTO) {

        OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(newOrderDetail);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("Create order detail successfully")
                .status(HttpStatus.CREATED)
                .data(orderDetailResponse).build());

    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrderDetail(
            @Valid @PathVariable("id") Long id) throws DataNotFoundException {
        OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
        OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(orderDetailResponse)
                .message("Success!")
                .build());
    }
    //lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ResponseObject> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ) {
        List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
        List<OrderDetailResponse> orderDetailResponses = orderDetails
                .stream()
                .map(OrderDetailResponse::fromOrderDetail)
                .toList();
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(orderDetailResponses)
                .message("Success!")
                .build());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @Valid @RequestBody OrderDetailDTO orderDetailDTO) {

        OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(orderDetail)
                .build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        orderDetailService.deleteById(id);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY)
                        .build());
    }
}
