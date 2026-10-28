package com.murillo.finsecure_api.service;

import com.murillo.finsecure_api.domain.enums.UserRole;
import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.domain.model.Wallet;
import com.murillo.finsecure_api.dto.request.LoginRequestDTO;
import com.murillo.finsecure_api.dto.request.RegisterRequestDTO;
import com.murillo.finsecure_api.dto.response.AuthResponseDTO;
import com.murillo.finsecure_api.repository.UserRepository;
import com.murillo.finsecure_api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        if (userRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new IllegalArgumentException("CPF/CNPJ já cadastrado no sistema");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .fullName(request.fullName())
                .cpfCnpj(request.cpfCnpj())
                .email(request.email())
                .passwordHash(passwordHash)
                .role(UserRole.ROLE_CLIENT)
                .build();

        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.ZERO) // Saldo inicial: R$ 0,00
                .currency("BRL")          // Moeda: Real Brasileiro
                .user(user)               // Diz que a carteira pertence a este usuário
                .build();

        user.setWallet(wallet); // Amarra a carteira de volta no usuário

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponseDTO.of(token, 86400000L);

    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),
                        request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        String token = jwtService.generateToken(user);
        return AuthResponseDTO.of(token, 86400000L);
    }
}
