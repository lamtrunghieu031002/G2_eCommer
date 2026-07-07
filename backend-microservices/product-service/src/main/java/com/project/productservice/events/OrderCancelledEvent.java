package com.project.productservice.events;

import java.util.List;

public record OrderCancelledEvent(Long orderId, List<Item> items) {
    public record Item(Long variantId, int quantity) {}
}
