package org.hackathon.genon.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hackathon.genon.domain.auth.controller.dto.LoginRequest;
import org.hackathon.genon.domain.auth.service.AuthService;
import org.hackathon.genon.global.security.jwt.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController extends AuthDocsController {

    private final AuthService authService;

    @Override
    @PostMapping("/v1/auth/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request.loginId(), request.password());

        return ResponseEntity.ok(response);
    }

}