package com.project.orderservice.services.orderDetail;

import com.project.orderservice.dtos.order.OrderDetailDTO;
import com.project.orderservice.exceptions.DataNotFoundException;
import com.project.orderservice.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail);
    OrderDetail getOrderDetail(Long id) throws DataNotFoundException;
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData)
            throws DataNotFoundException;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);


}
