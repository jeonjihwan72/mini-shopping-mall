package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.dto.OrderSimpleResponse;
import org.example.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal User user, @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(user.getUsername(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderSimpleResponse>> getOrders(@AuthenticationPrincipal User user, Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(user.getUsername(), pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal User user, @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId, user.getUsername()));
    }

}
