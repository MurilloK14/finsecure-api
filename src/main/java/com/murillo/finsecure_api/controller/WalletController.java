package com.murillo.finsecure_api.controller;

import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.dto.response.WalletResponseDTO;
import com.murillo.finsecure_api.repository.WalletRepository;
import com.murillo.finsecure_api.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
                public ResponseEntity<WalletResponseDTO>
        getMyWallet(@AuthenticationPrincipal User currentUser){
            WalletResponseDTO response = walletService.getMyWallet(currentUser);
            return ResponseEntity.ok(response);
        }
    }

