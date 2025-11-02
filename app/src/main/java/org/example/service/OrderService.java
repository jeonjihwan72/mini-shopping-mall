package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.dto.OrderSimpleResponse;
import org.example.entity.*;
import org.example.exception.BusinessException;
import org.example.repository.MemberRepository;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(String username, OrderRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
                    if (product.getStock() < item.getCount()) {
                        throw new BusinessException(HttpStatus.CONFLICT, "Not enough stock");
                    }
                    product.decreaseStock(item.getCount());
                    return OrderItem.builder()
                            .product(product)
                            .orderPrice(product.getPrice())
                            .count(item.getCount())
                            .build();
                })
                .collect(Collectors.toList());

        int totalPrice = orderItems.stream()
                .mapToInt(item -> item.getOrderPrice() * item.getCount())
                .sum();

        Order order = Order.builder()
                .member(member)
                .orderItems(orderItems)
                .totalPrice(totalPrice)
                .status(OrderStatus.ORDERED)
                .build();

        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    public Page<OrderSimpleResponse> getOrders(String username, Pageable pageable) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return orderRepository.findByMember(member, pageable).map(OrderSimpleResponse::from);
    }

    public OrderResponse getOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getMember().getUsername().equals(username)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to view this order");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.ORDERED) {
            throw new BusinessException(HttpStatus.CONFLICT, "Order is not in a state that can be canceled");
        }

        order.setStatus(OrderStatus.CANCELED);

        order.getOrderItems().forEach(orderItem -> {
            Product product = productRepository.findByIdWithPessimisticLock(orderItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
            product.increaseStock(orderItem.getCount());
        });
    }

    public Page<OrderSimpleResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderSimpleResponse::from);
    }
}
