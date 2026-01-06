package com.project.ecommerce.controllers;

import com.project.ecommerce.components.SecurityUtils;
import com.project.ecommerce.dtos.order.OrderDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.Order;
import com.project.ecommerce.models.User;
import com.project.ecommerce.responses.*;
import com.project.ecommerce.services.order.IOrderService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final SecurityUtils securityUtils;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> createOrder(
            @Valid @RequestBody OrderDTO orderDTO
    ) {
        Order orderResponse = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success!")
                .data(OrderResponse.fromOrder(orderResponse))
                .build());
    }
    @GetMapping("/user")
    //GET http://localhost:8089/api/v1/orders/user?status=&keyword=
    public ResponseEntity<ResponseObject> getOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        User loggedUser = securityUtils.getLoggedInUser();
        Long userId = loggedUser.getId();
        List<OrderResponse> orders = orderService.findOrders(userId, status, keyword);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success!")
                .data(orders)
                .build());
    }
    //GET http://localhost:8088/api/v1/orders/2
    @GetMapping("/{id}")
    @PostAuthorize("hasRole('ROLE_ADMIN') or returnObject.body.data.userId == authentication.principal.id")
    public ResponseEntity<ResponseObject> getOrder(@Valid @PathVariable("id") Long orderId) {
        Order existingOrder = orderService.getOrderById(orderId);
        OrderResponse orderResponse = OrderResponse.fromOrder(existingOrder);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Success!")
                .data(orderResponse).build());
    }
    @PutMapping("/{id}")
    //PUT http://localhost:8088/api/v1/orders/2
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateOrder(
            @Valid @PathVariable long id,
            @Valid @RequestBody OrderDTO orderDTO) {

        Order order = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Update Success!")
                .data(order)
                .build());
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteOrder(@Valid @PathVariable Long id) {
        //xóa mềm => cập nhật trường active = false
        orderService.deleteOrder(id);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(MessageKeys.DELETE_ORDER_SUCCESSFULLY)
                .build());
    }
    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getOrdersByKeyword(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );

        Page<OrderResponse> orderPage = orderService
                                        .getOrdersByKeyword(keyword, pageRequest);

        // Lấy tổng số trang
        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orderResponses = orderPage.getContent();
        OrderListResponse orderListResponse = OrderListResponse.builder()
                .totalPages(totalPages)
                .orders(orderResponses).build();
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .data(orderListResponse)
                .build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateOrderStatus(
            @Valid @PathVariable Long id,
            @RequestParam String status) {
        // Gọi service để cập nhật trạng thái
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        // Trả về phản hồi thành công
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Order status updated successfully!")
                .status(HttpStatus.OK)
                .data(OrderResponse.fromOrder(updatedOrder))
                .build());
    }
}
