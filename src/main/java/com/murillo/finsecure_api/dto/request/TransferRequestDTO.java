package com.murillo.finsecure_api.dto.request;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public record TransferRequestDTO(
        @NotNull(message = "O ID da carteira de destino é obrigatório")
        UUID receiverWalletId,

        @NotNull(message = "O valor da transferêncua é obrigatório")
        @Positive(message = "O valor da transferência deve ser maior que zero")

        BigDecimal amount,

        @NotBlank(message = "A chave de idempotência é obrigatória")
        String idempotencyKey
        ) {}
