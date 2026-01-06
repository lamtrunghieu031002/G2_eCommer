package com.project.ecommerce.models;

import java.util.Set;

public class OrderStatus {
    public static final String PENDING = "pending";
    public static final String PROCESSING = "processing";
    public static final String SHIPPING = "shipping";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";
    public static final String RETURNED = "returned";
    public static final Set<String> VALID_STATUSES = Set.of(
            PENDING, PROCESSING, SHIPPING, DELIVERED, CANCELLED, RETURNED
    );
}
