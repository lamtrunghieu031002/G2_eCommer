package com.project.productservice.events;


import com.project.productservice.repositories.VariantRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {
    private final VariantRepository variantRepository;
    @KafkaListener(topics = "order-created",groupId = "product-service")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event){
        log.info("Nhan event order-created, orderId={}",event.orderId());
        for (OrderCreatedEvent.Item item : event.items()) {
            int updated = variantRepository.decreaseStock(item.variantId(), item.quantity());
            if (updated == 0) {
                log.warn("Khong tru duoc kho variant {} (het hang?)", item.variantId());
            }
        }
    }
    @KafkaListener(topics = "order-cancelled", groupId = "product-service")
    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Nhan event order-cancelled, orderId={}", event.orderId());
        for (OrderCancelledEvent.Item item : event.items()) {
            variantRepository.increaseStock(item.variantId(), item.quantity());
        }
    }
}
