package com.murillo.finsecure_api.service;

import com.murillo.finsecure_api.config.RabbitMQConfig;
import com.murillo.finsecure_api.domain.enums.TransactionStatus;
import com.murillo.finsecure_api.domain.model.AuditLog;
import com.murillo.finsecure_api.domain.model.Transaction;
import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.domain.model.Wallet;
import com.murillo.finsecure_api.dto.request.TransferRequestDTO;
import com.murillo.finsecure_api.dto.response.TransferResponseDTO;
import com.murillo.finsecure_api.event.TransactionNotificationEvent;
import com.murillo.finsecure_api.repository.AuditLogRepository;
import com.murillo.finsecure_api.repository.TransactionalRepository;
import com.murillo.finsecure_api.repository.UserRepository;
import com.murillo.finsecure_api.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    WalletRepository walletRepository;

    @Mock
    AuditLogRepository auditLogRepository;

    @Mock
    TransactionalRepository transactionalRepository;

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    TransferService transferService;

    @Test
    void transfer_DeveTransferirComSucesso_QuandoSaldoForSuficiente() {
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        Wallet senderWallet = new Wallet().builder()
                .id(senderWalletId)
                .balance(BigDecimal.valueOf(500))
                .currency("BRL")
                .build();

        Wallet receiverWallet = new Wallet().builder()
                .id(receiverWalletId)
                .balance(BigDecimal.valueOf(100))
                .currency("BRL")
                .build();

        TransferRequestDTO request = new TransferRequestDTO(
                receiverWalletId,
                BigDecimal.valueOf(100),
                "tx-123");

        User senderUser = new User().builder()
                .wallet(senderWallet)
                .email("murillo@gmail.com")
                .cpfCnpj("61151275131")
                .fullName("Murillo Kennedy")
                .build();

        User receiverUser = new User().builder()
                .wallet(receiverWallet)
                .email("caio@gmail.com")
                .cpfCnpj("64441955359")
                .fullName("Caio Vinicius")
                .build();

        when(transactionalRepository.existsByIdempotencyKey(any())).thenReturn(false);

        when(walletRepository.findByIdWithPessimisticLock(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIdWithPessimisticLock(receiverWalletId)).thenReturn(Optional.of(receiverWallet));

        when(transactionalRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    return tx;
                });

        TransferResponseDTO response = transferService.transfer(senderUser, request);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
        Assertions.assertThat(response.amount()).isEqualTo(request.amount());
        Assertions.assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("400"));
        Assertions.assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("200"));
        verify(transactionalRepository, times(1)).save(any(Transaction.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.NOTIFICATION_ROUTING_KEY),
                any(TransactionNotificationEvent.class)
        );
    }

    @Test
    void transfer_DeveLancarExcecao_QuandoSaldoForInsuficiente() {
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        Wallet senderWallet = new Wallet().builder()
                .id(senderWalletId)
                .balance(BigDecimal.valueOf(50))
                .currency("BRL")
                .build();

        Wallet receiverWallet = new Wallet().builder()
                .id(receiverWalletId)
                .balance(BigDecimal.valueOf(100))
                .currency("BRL")
                .build();

        TransferRequestDTO request = new TransferRequestDTO(
                receiverWalletId,
                BigDecimal.valueOf(100),
                "tx-123");

        User senderUser = new User().builder()
                .wallet(senderWallet)
                .email("murillo@gmail.com")
                .cpfCnpj("61151275131")
                .fullName("Murillo Kennedy")
                .build();

        User receiverUser = new User().builder()
                .wallet(receiverWallet)
                .email("caio@gmail.com")
                .cpfCnpj("64441955359")
                .fullName("Caio Vinicius")
                .build();

        when(transactionalRepository.existsByIdempotencyKey(any())).thenReturn(false);

        when(walletRepository.findByIdWithPessimisticLock(senderWalletId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByIdWithPessimisticLock(receiverWalletId)).thenReturn(Optional.of(receiverWallet));

        Assertions.assertThatThrownBy(() -> transferService.transfer(senderUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saldo insuficiente para realizar a transação");

        Assertions.assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("50"));
        Assertions.assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100"));
        verify(transactionalRepository, never()).save(any(Transaction.class));
        verify(auditLogRepository, never()).save(any(AuditLog.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void transfer_DeveLancarExcecao_QuandoChaveIdempotenciaJaExistir() {
        UUID senderWalletId = UUID.randomUUID();
        UUID receiverWalletId = UUID.randomUUID();

        Wallet senderWallet = new Wallet().builder()
                .id(senderWalletId)
                .balance(BigDecimal.valueOf(500))
                .currency("BRL")
                .build();

        Wallet receiverWallet = new Wallet().builder()
                .id(receiverWalletId)
                .balance(BigDecimal.valueOf(100))
                .currency("BRL")
                .build();

        TransferRequestDTO request = new TransferRequestDTO(
                receiverWalletId,
                BigDecimal.valueOf(100),
                "tx-123");

        User senderUser = new User().builder()
                .wallet(senderWallet)
                .email("murillo@gmail.com")
                .cpfCnpj("61151275131")
                .fullName("Murillo Kennedy")
                .build();

        User receiverUser = new User().builder()
                .wallet(receiverWallet)
                .email("caio@gmail.com")
                .cpfCnpj("64441955359")
                .fullName("Caio Vinicius")
                .build();

        when(transactionalRepository.existsByIdempotencyKey(any())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> transferService.transfer(senderUser, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transação já processada chave duplicada");

        Assertions.assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("500"));
        Assertions.assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100"));
        verify(walletRepository, never()).findByIdWithPessimisticLock(any());
        verify(transactionalRepository, never()).save(any(Transaction.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
