package com.murillo.finsecure_api.controller;

import com.murillo.finsecure_api.dto.request.LoginRequestDTO;
import com.murillo.finsecure_api.dto.request.RegisterRequestDTO;
import com.murillo.finsecure_api.dto.response.AuthResponseDTO;
import com.murillo.finsecure_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid@RequestBody RegisterRequestDTO request) {
        AuthResponseDTO register = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(register);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid  @RequestBody LoginRequestDTO request) {
        AuthResponseDTO login = authService.login(request);
        return ResponseEntity.ok(login);
    }

}

