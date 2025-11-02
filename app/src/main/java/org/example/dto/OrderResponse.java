package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.entity.Order;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String memberUsername;
    private List<OrderItemResponse> orderItems;
    private int totalPrice;
    private String status;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .memberUsername(order.getMember().getUsername())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .build();
    }
}
