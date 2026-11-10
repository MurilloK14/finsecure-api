package com.murillo.finsecure_api.service;

import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.domain.model.Wallet;
import com.murillo.finsecure_api.dto.response.WalletResponseDTO;
import com.murillo.finsecure_api.repository.WalletRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Cacheable(value = "wallets", key = "#currentUser.id")
    public WalletResponseDTO getMyWallet(User currentUser) {
        Wallet wallet = walletRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new
                        IllegalArgumentException("Carteira não encontrada para o usuário"));
        return new WalletResponseDTO(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCurrency()
        );
    }

    @CacheEvict(value = "wallets",  key =  "#userId")
    public void evictWalletCache(UUID userId){

    }
}
