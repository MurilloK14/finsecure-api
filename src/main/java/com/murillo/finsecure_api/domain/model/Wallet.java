package com.murillo.finsecure_api.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tb_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable=false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void debit(BigDecimal amount){
        if ( amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 ){
            throw new IllegalArgumentException("o valor do débito deve ser maior que zero");
        }
        if(this.balance.compareTo(amount) < 0){
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transação");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount){
        if ( amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 ){
            throw new IllegalArgumentException("O valor eo crédito deve ser maior que zero");
        }
        this.balance = this.balance.add(amount);
    }

}

