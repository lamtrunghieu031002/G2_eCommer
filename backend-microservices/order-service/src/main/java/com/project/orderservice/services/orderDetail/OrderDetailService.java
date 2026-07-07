package com.project.orderservice.services.orderDetail;

import com.project.orderservice.clients.ProductClient;
import com.project.orderservice.dtos.order.OrderDetailDTO;
import com.project.orderservice.exceptions.DataNotFoundException;
import com.project.orderservice.models.Order;
import com.project.orderservice.models.OrderDetail;
import com.project.orderservice.repositories.OrderDetailRepository;
import com.project.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderDetailService implements IOrderDetailService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) {
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Order with id : " + orderDetailDTO.getOrderId()));
        ProductClient.VariantInfo variant = productClient.getVariant(orderDetailDTO.getVariantId());
        if (variant == null) {
            throw new DataNotFoundException(
                    "Cannot find product with id: " + orderDetailDTO.getVariantId());
        }
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .variantId(orderDetailDTO.getVariantId())
                .productId(variant.productId())
                .productName(variant.productName())
                .variantName(variant.variantName())
                .thumbnail(variant.thumbnail())
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .build();
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(Long id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + id));
    }

    @Override
    @Transactional
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO)
            throws DataNotFoundException {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id: " + id));
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + id));
        ProductClient.VariantInfo variant = productClient.getVariant(orderDetailDTO.getVariantId());
        if (variant == null) {
            throw new DataNotFoundException(
                    "Cannot find product with id: " + orderDetailDTO.getVariantId());
        }
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setVariantId(orderDetailDTO.getVariantId());
        existingOrderDetail.setProductId(variant.productId());
        existingOrderDetail.setProductName(variant.productName());
        existingOrderDetail.setVariantName(variant.variantName());
        existingOrderDetail.setThumbnail(variant.thumbnail());
        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
