package com.murillo.finsecure_api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de mensageria com RabbitMQ (AMQP).
 * Define Exchanges, Filas (Queues), Bindings, Conversor JSON e Resiliência com Dead Letter Queue (DLQ).
 */
@Configuration
public class RabbitMQConfig {

    // Nomes da infraestrutura principal
    public static final String EXCHANGE_NAME = "finsecure.events.exchange";
    public static final String NOTIFICATION_QUEUE = "finsecure.transaction-notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "transaction.notification";

    // Nomes da infraestrutura de Dead Letter (DLX / DLQ)
    public static final String DLX_NAME = "finsecure.events.dlx";
    public static final String NOTIFICATION_DLQ = "finsecure.transaction-notification.dlq";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "transaction.notification.dlq";

    /**
     * 1. Fila Principal de Notificações (durável).
     */
    @Bean
    public Queue transactionNotificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /**
     * 2. Direct Exchange Principal do FinSecure.
     */
    @Bean
    public DirectExchange eventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /**
     * 3. Binding da Fila Principal à Exchange Principal.
     */
    @Bean
    public Binding transactionNotificationBinding(Queue transactionNotificationQueue, DirectExchange eventsExchange) {
        return BindingBuilder
                .bind(transactionNotificationQueue)
                .to(eventsExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 4. Dead Letter Queue (DLQ):
     * Fila para onde são enviadas as mensagens que falharam após todas as tentativas de reprocessamento.
     */
    @Bean
    public Queue transactionNotificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    /**
     * 5. Dead Letter Exchange (DLX):
     * Exchange dedicada exclusivamente para rotear mensagens mortas (falhas).
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    /**
     * 6. Binding da DLQ à DLX.
     */
    @Bean
    public Binding transactionNotificationDlqBinding(Queue transactionNotificationDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(transactionNotificationDlq)
                .to(deadLetterExchange)
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    /**
     * 7. MessageRecoverer (RepublishMessageRecoverer):
     * Quando o Consumer falha 3 vezes, este interceptor republica a mensagem na DLX com o motivo do erro.
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DLX_NAME, NOTIFICATION_DLQ_ROUTING_KEY);
    }

    /**
     * 8. Conversor de Mensagens Jackson (JSON UTF-8).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 9. RabbitTemplate customizado com o MessageConverter JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    /**
     * 10. RabbitAdmin: Gerencia a criação das entidades AMQP.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    /**
     * 11. Inicializador de Conexão: força o registro imediato das filas no RabbitMQ.
     */
    @Bean
    public ApplicationRunner initRabbitDeclarations(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}
