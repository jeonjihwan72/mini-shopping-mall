package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.entity.Order;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OrderSimpleResponse {
    private Long id;
    private int totalPrice;
    private String status;
    private LocalDateTime orderDate;

    public static OrderSimpleResponse from(Order order) {
        return OrderSimpleResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .orderDate(order.getCreatedDate())
                .build();
    }
}
