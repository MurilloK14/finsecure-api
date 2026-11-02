package com.murillo.finsecure_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponseDTO(
        UUID walletID,
        BigDecimal balance,
        String currency
) {
}
