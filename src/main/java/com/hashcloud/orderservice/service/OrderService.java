package com.hashcloud.orderservice.service;

import com.hashcloud.orderservice.client.ProductServiceClient;
import com.hashcloud.orderservice.dto.CreateOrderRequest;
import com.hashcloud.orderservice.dto.OrderResponse;
import com.hashcloud.orderservice.dto.ProductResponse;
import com.hashcloud.orderservice.entity.Order;
import com.hashcloud.orderservice.entity.OrderItem;
import com.hashcloud.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderService(
            OrderRepository orderRepository,
            ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request == null || request.customerEmail() == null
                || request.customerEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required");
        }

        Order order = new Order();
        order.setCustomerEmail(request.customerEmail().trim());
        order.setStatus("CREATED");

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest requestedItem : request.items()) {
            if (requestedItem == null || requestedItem.productId() == null) {
                throw new IllegalArgumentException("Product ID is required");
            }

            if (requestedItem.quantity() == null || requestedItem.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }

            ProductResponse product =
                    productServiceClient.getProductById(requestedItem.productId());

            if (product == null || !Boolean.TRUE.equals(product.is_active())) {
                throw new IllegalArgumentException("Product is unavailable");
            }

            if (product.stock() == null
                    || requestedItem.quantity() > product.stock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.id());
            }

            if (product.price() == null || product.price().signum() <= 0) {
                throw new IllegalArgumentException(
                        "Invalid price for product: " + product.id());
            }

            BigDecimal subtotal = product.price()
                    .multiply(BigDecimal.valueOf(requestedItem.quantity()));

            OrderItem item = OrderItem.create(
                    order,
                    product.id(),
                    product.name(),
                    requestedItem.quantity(),
                    product.price());

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items);
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(Long id) {
            super("Order not found: " + id);
        }
    }
}
