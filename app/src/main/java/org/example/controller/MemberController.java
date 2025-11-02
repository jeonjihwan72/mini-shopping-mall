package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.MemberJoinRequest;
import org.example.dto.MemberJoinResponse;
import org.example.dto.MemberLoginRequest;
import org.example.dto.TokenResponse;
import org.example.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<MemberJoinResponse> join(@RequestBody MemberJoinRequest request) {
        return ResponseEntity.ok(memberService.join(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody MemberLoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));
    }
}
