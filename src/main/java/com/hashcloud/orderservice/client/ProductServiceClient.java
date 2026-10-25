package com.hashcloud.orderservice.client;

import com.hashcloud.orderservice.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(
            RestClient.Builder builder,
            @Value("${product.service.url}") String productServiceUrl) {

        this.restClient = builder
                .baseUrl(productServiceUrl)
                .build();
    }

    public ProductResponse getProductById(Long productId) {
        return restClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .body(ProductResponse.class);
    }
}
