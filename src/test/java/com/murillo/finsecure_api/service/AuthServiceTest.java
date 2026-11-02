package com.murillo.finsecure_api.service;

import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.dto.request.LoginRequestDTO;
import com.murillo.finsecure_api.dto.request.RegisterRequestDTO;
import com.murillo.finsecure_api.dto.response.AuthResponseDTO;
import com.murillo.finsecure_api.repository.UserRepository;
import com.murillo.finsecure_api.security.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_DeveCadastrarUsuarioComSucesso_QuandoDadosForemValidos() {
       RegisterRequestDTO request = new RegisterRequestDTO(
               "Murillo Kennedy",
               "52998224725",
               "murillo@gmail.com",
               "senhaForte123"
       );

       when(userRepository.existsByEmail(request.email())).thenReturn(false);
       when(userRepository.existsByCpfCnpj(request.cpfCnpj())).thenReturn(false);
       when(passwordEncoder.encode(request.password())).thenReturn("hash_criptografado_123");
       when(jwtService.generateToken(any())).thenReturn("token_jwt_valido");

       AuthResponseDTO response = authService.register(request);

       Assertions.assertThat(response).isNotNull();
       Assertions.assertThat(response.token()).isEqualTo("token_jwt_valido");
       Assertions.assertThat(response.tokenType()).isEqualTo("Bearer");
       verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DeveLancarExcecao_QuandoEmailJaEstiverCadastrado() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Murillo Kennedy",
                "52998224725",
                "murillo@gmail.com",
                "senhaForte123"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já cadastrado no sistema");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void login_DeveAutenticarERetornarToken_QuandoCredenciaisForemValidas() {
        LoginRequestDTO login = new LoginRequestDTO(
                "murillo@gmail.com",
                "senhaForte123"
        );

        User user = User.builder()
                .fullName("Murillo Kennedy")
                .cpfCnpj("52998224725")
                .email("murillo@gmail.com")
                .passwordHash("hash_criptografado_123")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(null);
        when(userRepository.findByEmail(login.email()))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("token_jwt_valido");

        AuthResponseDTO response = authService.login(login);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isEqualTo("token_jwt_valido");
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByEmail(login.email());
    }
}
