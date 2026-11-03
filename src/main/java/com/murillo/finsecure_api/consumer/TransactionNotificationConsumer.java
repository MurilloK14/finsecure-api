package com.murillo.finsecure_api.consumer;

import com.murillo.finsecure_api.config.RabbitMQConfig;
import com.murillo.finsecure_api.event.TransactionNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor assíncrono de notificações de transferências via RabbitMQ.
 * Escuta a fila 'finsecure.transaction-notification.queue' em segundo plano.
 * Ao concluir a execução sem erros, o Spring envia um ACK automático para o RabbitMQ remover a mensagem da fila.
 */
@Component
public class TransactionNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionNotificationConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeTransactionNotification(TransactionNotificationEvent event) {
        log.info("=================================================================");
        log.info("📩 [MENSAGEM RECEBIDA DO RABBITMQ]");
        log.info("Transação ID: {}", event.transactionId());
        log.info("Valor: R$ {}", event.amount());
        log.info("Origem: Carteira {} -> Destino: Carteira {}", event.senderWalletId(), event.receiverWalletId());
        log.info("Timestamp do Evento: {}", event.createdAt());

        // Simulação do processamento de envio de notificação (Push Notification / E-mail)
        log.info("🚀 Enviando comprovante e notificação Push para o titular da carteira de destino...");
        log.info("✅ Notificação processada com sucesso! ACK enviado ao RabbitMQ.");
        log.info("=================================================================");
    }
}
