package com.murillo.finsecure_api.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento assíncrono publicado no RabbitMQ após a conclusão atômica de uma transferência.
 * Representa os dados necessários para o envio de notificações e comprovantes sem bloquear a requisição HTTP.
 */
public record TransactionNotificationEvent(
        UUID transactionId,
        UUID senderWalletId,
        UUID receiverWalletId,
        BigDecimal amount,
        Instant createdAt
) {
}
