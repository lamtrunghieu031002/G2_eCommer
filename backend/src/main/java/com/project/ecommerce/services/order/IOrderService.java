package com.project.ecommerce.services.order;

import com.project.ecommerce.dtos.order.OrderDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.Order;
import com.project.ecommerce.responses.OrderResponse;
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
