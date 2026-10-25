
package com.hashcloud.orderservice.repository;

import com.hashcloud.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
