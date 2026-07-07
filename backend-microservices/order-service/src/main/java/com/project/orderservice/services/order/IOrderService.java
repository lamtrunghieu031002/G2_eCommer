package com.project.orderservice.services.order;

import com.project.orderservice.dtos.order.OrderDTO;
import com.project.orderservice.exceptions.DataNotFoundException;
import com.project.orderservice.models.Order;
import com.project.orderservice.responses.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long id);
    Order updateOrder(Long id, OrderDTO orderDTO);
    void deleteOrder(Long id);
    List<OrderResponse> findOrders(Long userId, String status, String keyword);
    Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable);
    Order updateOrderStatus( Long id, String status);
}
