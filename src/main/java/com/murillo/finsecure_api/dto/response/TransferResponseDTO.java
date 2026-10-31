package com.murillo.finsecure_api.dto.response;

import com.murillo.finsecure_api.domain.enums.TransactionStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CurrentTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponseDTO(
        UUID transactionId,
        TransactionStatus status,
        BigDecimal amount,
        LocalDateTime createdAt,
        String message
) {}
