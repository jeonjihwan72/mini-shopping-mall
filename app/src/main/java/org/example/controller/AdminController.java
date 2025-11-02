package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.RoleUpdateRequest;
import org.example.service.MemberService;
import org.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final MemberService memberService;

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/members/role")
    public ResponseEntity<Void> updateMemberRole(@RequestBody RoleUpdateRequest request) {
        memberService.updateMemberRole(request.getUsername(), request.getNewRole());
        return ResponseEntity.noContent().build();
    }
}
