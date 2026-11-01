package com.murillo.finsecure_api.dto.request;

import com.murillo.finsecure_api.validation.CpfOrCnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequestDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres")
        String fullName,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        @CpfOrCnpj(message = "CPF ou CNPJ inválido")
        String cpfCnpj,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password
) {}