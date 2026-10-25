package com.hashcloud.orderservice.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        Integer stock,
        Boolean is_active
) {
}
