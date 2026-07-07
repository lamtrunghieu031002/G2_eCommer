package com.project.orderservice.services.order;

import com.project.orderservice.clients.ProductClient;
import com.project.orderservice.clients.UserClient;
import com.project.orderservice.dtos.order.CartItemDTO;
import com.project.orderservice.dtos.order.OrderDTO;
import com.project.orderservice.dtos.order.OrderWithDetailsDTO;
import com.project.orderservice.events.OrderCancelledEvent;
import com.project.orderservice.events.OrderCreatedEvent;
import com.project.orderservice.exceptions.DataNotFoundException;
import com.project.orderservice.models.*;
import com.project.orderservice.repositories.*;
import com.project.orderservice.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        // kiem tra user ton tai (goi user-service)
        Map<String, Object> user = userClient.getUser(orderDTO.getUserId());
        if (user == null) {
            throw new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId());
        }
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUserId(orderDTO.getUserId());
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);
        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? LocalDate.now().plusDays(3) : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today !");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        if (orderDTO.getShippingAddress() == null) {
            order.setShippingAddress((String) user.get("address"));
        } else {
            order.setShippingAddress(orderDTO.getShippingAddress());
        }

        orderRepository.save(order);
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);

            Long variantId = cartItemDTO.getVariantId();
            int quantity = cartItemDTO.getQuantity();

            // lay thong tin variant tu product-service, luu snapshot vao order_details
            ProductClient.VariantInfo variant = productClient.getVariant(variantId);
            if (variant == null) {
                throw new DataNotFoundException("Product not found with id: " + variantId);
            }
            orderDetail.setVariantId(variantId);
            orderDetail.setProductId(variant.productId());
            orderDetail.setProductName(variant.productName());
            orderDetail.setVariantName(variant.variantName());
            orderDetail.setThumbnail(variant.thumbnail());
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(variant.productPrice());
            orderDetail.setTotalMoney(orderDetail.getPrice() * orderDetail.getNumberOfProducts());

            orderDetails.add(orderDetail);
        }

        order.setOrderDetails(orderDetails);
        orderDetailRepository.saveAll(orderDetails);
        // phat event de product-service tru ton kho (bat dong bo)
        List<OrderCreatedEvent.Item>items=orderDetails.stream()
                .map(od->new OrderCreatedEvent.Item(od.getVariantId(),
                        od.getNumberOfProducts()
                        )).toList();
        kafkaTemplate.send("order-created", String.valueOf(order.getId()),
                new OrderCreatedEvent(order.getId(), items));
        return order;
    }

    @Transactional
    public Order updateOrderWithDetails(OrderWithDetailsDTO orderWithDetailsDTO) {
        modelMapper.typeMap(OrderWithDetailsDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderWithDetailsDTO, order);
        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(order.getOrderDetails());
        savedOrder.setOrderDetails(savedOrderDetails);

        return savedOrder;
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            order = orderRepository.findByVnpTxnRef(orderId.toString()).orElse(null);
        }
        return order;
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO)
            throws DataNotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find order with id: " + id));
        if (userClient.getUser(orderDTO.getUserId()) == null) {
            throw new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId());
        }
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        modelMapper.map(orderDTO, order);
        order.setUserId(orderDTO.getUserId());

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        // soft-delete
        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<OrderResponse> findOrders(Long userId, String status, String keyword) {
        List<Order> orders = orderRepository.findOrders(userId, status, keyword);
        return orders.stream().map(OrderResponse::fromOrder).toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable) {
        Page<Order> orderPages;
        orderPages = orderRepository.findAll(keyword, pageable);
        return orderPages.map(OrderResponse::fromOrder);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (!OrderStatus.VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        String currentStatus = order.getStatus();
        if (currentStatus.equals(OrderStatus.DELIVERED) && !status.equals(OrderStatus.RETURNED)) {
            throw new IllegalArgumentException("Cannot change status from DELIVERED to " + status);
        }
        if (currentStatus.equals(OrderStatus.CANCELLED)) {
            throw new IllegalArgumentException("Cannot change status of a CANCELLED order");
        }
        if (status.equals(OrderStatus.CANCELLED) && !currentStatus.equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Order can only be cancelled from PENDING status");
        }

        order.setStatus(status);
        Order saved=orderRepository.save(order);
        // don bi huy -> phat event de product-service hoan lai ton kho
        if(OrderStatus.CANCELLED.equals(status)){
            List<OrderDetail>details=
                    orderDetailRepository.findByOrderId(saved.getId());
            List<OrderCancelledEvent.Item> items=details.stream()
                    .map(od->new OrderCancelledEvent.Item(od.getVariantId(),
                            od.getNumberOfProducts()))
                    .toList();
            kafkaTemplate.send("order-cancelled",String.valueOf(saved.getId()),
                    new OrderCancelledEvent(saved.getId(),items));
        }

        return saved;
//        order.setStatus(status);
//        return orderRepository.save(order);
    }
//    List<OrderCreatedEvent.Item>items=orderDetailRepository
}
