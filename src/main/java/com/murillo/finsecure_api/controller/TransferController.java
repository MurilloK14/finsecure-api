package com.murillo.finsecure_api.controller;

import com.murillo.finsecure_api.domain.model.User;
import com.murillo.finsecure_api.dto.request.TransferRequestDTO;
import com.murillo.finsecure_api.dto.response.TransferResponseDTO;
import com.murillo.finsecure_api.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {


    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransfer(
        @AuthenticationPrincipal User currentUser,
        @Valid @RequestBody TransferRequestDTO request
    ){
    TransferResponseDTO response = transferService.transfer(currentUser, request);
    return ResponseEntity.ok(response);
        }

}
