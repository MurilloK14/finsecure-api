package com.murillo.finsecure_api.service;

import com.murillo.finsecure_api.domain.enums.TransactionStatus;
import com.murillo.finsecure_api.domain.model.AuditLog;
import com.murillo.finsecure_api.domain.model.Transaction;
import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.domain.model.Wallet;
import com.murillo.finsecure_api.dto.request.TransferRequestDTO;
import com.murillo.finsecure_api.dto.response.TransferResponseDTO;
import com.murillo.finsecure_api.repository.AuditLogRepository;
import com.murillo.finsecure_api.repository.TransactionalRepository;
import com.murillo.finsecure_api.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionalRepository transactionalRepository;


    public TransferService(WalletRepository walletRepository, AuditLogRepository auditLogRepository, TransactionalRepository transactionalRepository) {
        this.walletRepository = walletRepository;
        this.auditLogRepository = auditLogRepository;
        this.transactionalRepository = transactionalRepository;
    }


    @Transactional
    public TransferResponseDTO transfer(User sendUser, TransferRequestDTO request) {

        UUID senderWalletId = sendUser.getWallet().getId();
        UUID receiverWalletId = request.receiverWalletId();

        Wallet senderWallet;
        Wallet receiverWallet;

        if (transactionalRepository.existsByIdempotencyKey(request.idempotencyKey())) {
            throw new IllegalArgumentException("Transação já processada chave duplicada");
        }
        if(senderWalletId.equals(receiverWalletId)){
            throw new IllegalArgumentException("Não é possível transferir para si mesmo");
        }

        if(senderWalletId.compareTo(receiverWalletId) < 0){
            senderWallet =
                    walletRepository.findByIdWithPessimisticLock(senderWalletId)
                    .orElseThrow(() -> new  IllegalArgumentException("Carteira de origem não encontrada"));

            receiverWallet =
                    walletRepository.findByIdWithPessimisticLock(receiverWalletId)
                    .orElseThrow(() -> new IllegalArgumentException("Carteira de destino não encontrada"));

        } else {

            receiverWallet =
                    walletRepository.findByIdWithPessimisticLock(receiverWalletId)
                            .orElseThrow(() -> new IllegalArgumentException("Carteira de destino não encontrada"));

            senderWallet =
                    walletRepository.findByIdWithPessimisticLock(senderWalletId)
                            .orElseThrow(() -> new  IllegalArgumentException("Carteira de origem não encontrada"));

        }

        senderWallet.debit(request.amount());
        receiverWallet.credit(request.amount());

        Transaction transaction = Transaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(request.amount())
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(request.idempotencyKey())
                .build();
        transaction = transactionalRepository.save(transaction);

        AuditLog auditLog = AuditLog.builder()
                .entityName("TRANSACTION")
                .entityId(transaction.getId().toString())
                .action("TRANSFER")
                .userEmail(sendUser.getEmail())
                .details(String.format("Transferência de R$ %s para carteira %s",
                        request.amount(), receiverWalletId))
                .build();

        auditLogRepository.save(auditLog);

        return new TransferResponseDTO(
                transaction.getId(),
                transaction.getStatus(),
                transaction.getAmount(),
                LocalDateTime.now(),
                "Transferência realizada com sucesso"
        );
    }
}
