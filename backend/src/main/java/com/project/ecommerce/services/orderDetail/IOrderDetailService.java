package com.project.ecommerce.services.orderDetail;

import com.project.ecommerce.dtos.order.OrderDetailDTO;
import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail);
    OrderDetail getOrderDetail(Long id) throws DataNotFoundException;
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData)
            throws DataNotFoundException;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId);


}
