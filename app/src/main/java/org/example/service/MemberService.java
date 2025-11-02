package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.config.JwtTokenProvider;
import org.example.dto.MemberJoinRequest;
import org.example.dto.MemberJoinResponse;
import org.example.dto.MemberLoginRequest;
import org.example.dto.TokenResponse;
import org.example.entity.Member;
import org.example.entity.Role;
import org.example.exception.BusinessException;
import org.example.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberJoinResponse join(MemberJoinRequest request) {
        if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username already exists");
        }

        Role assignedRole = request.getUsername().toLowerCase().contains("admin") ? Role.ADMIN : Role.USER;

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .role(assignedRole)
                .build();

        memberRepository.save(member);
        return new MemberJoinResponse(member.getUsername());
    }

    public TokenResponse login(MemberLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        return new TokenResponse(jwtTokenProvider.createToken(authentication));
    }

    @Transactional
    public void updateMemberRole(String username, Role newRole) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Member not found"));
        member.updateRole(newRole);
    }
}
