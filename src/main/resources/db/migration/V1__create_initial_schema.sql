-- Habilita extensão para geração de UUIDs nativos no PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. TABELA DE USUÁRIOS
CREATE TABLE tb_users (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          full_name VARCHAR(150) NOT NULL,
                          cpf_cnpj VARCHAR(18) NOT NULL UNIQUE,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CLIENT',
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABELA DE CARTEIRAS (WALLETS)
CREATE TABLE tb_wallets (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL UNIQUE,
                            balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
                            currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
                            version BIGINT NOT NULL DEFAULT 0,
                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES tb_users (id) ON DELETE CASCADE,
                            CONSTRAINT chk_wallet_balance_positive CHECK (balance >= 0)
);

-- 3. TABELA DE TRANSAÇÕES FINANCEIRAS
CREATE TABLE tb_transactions (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 sender_wallet_id UUID NOT NULL,
                                 receiver_wallet_id UUID NOT NULL,
                                 amount NUMERIC(15, 2) NOT NULL,
                                 status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                 idempotency_key VARCHAR(100) UNIQUE,
                                 failure_reason VARCHAR(255),
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_tx_sender FOREIGN KEY (sender_wallet_id) REFERENCES tb_wallets (id),
                                 CONSTRAINT fk_tx_receiver FOREIGN KEY (receiver_wallet_id) REFERENCES tb_wallets (id),
                                 CONSTRAINT chk_tx_amount_positive CHECK (amount > 0)
);

-- 4. TABELA DE AUDITORIA IMUTÁVEL
CREATE TABLE tb_audit_logs (
                               id BIGSERIAL PRIMARY KEY,
                               entity_name VARCHAR(50) NOT NULL,
                               entity_id VARCHAR(100) NOT NULL,
                               action VARCHAR(50) NOT NULL,
                               user_email VARCHAR(150),
                               ip_address VARCHAR(45),
                               details TEXT,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ÍNDICES PARA ALTA PERFORMANCE
CREATE INDEX idx_users_email ON tb_users (email);
CREATE INDEX idx_users_cpf_cnpj ON tb_users (cpf_cnpj);
CREATE INDEX idx_wallets_user ON tb_wallets (user_id);
CREATE INDEX idx_tx_sender ON tb_transactions (sender_wallet_id);
CREATE INDEX idx_tx_receiver ON tb_transactions (receiver_wallet_id);
CREATE INDEX idx_tx_idempotency ON tb_transactions (idempotency_key);