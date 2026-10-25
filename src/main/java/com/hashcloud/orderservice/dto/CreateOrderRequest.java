package com.hashcloud.orderservice.dto;

import java.util.List;

public record CreateOrderRequest(
        String customerEmail,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {
    }
}
