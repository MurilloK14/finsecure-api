import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. Configuração do Estresse: 10 requisições simultâneas
export const options = {
    vus: 5,           // 5 usuários virtuais disparando juntos
    iterations: 10,   // Total de 10 transferências
};

// 2. Setup: Roda 1 vez antes do teste para logar a Alice e pegar o Token JWT
export function setup() {
    const loginUrl = 'http://localhost:8080/api/v1/auth/login';
    const payload = JSON.stringify({
        email: 'alice@email.com',
        password: 'senhaForte123' // Senha padrão cadastrada
    });

    const res = http.post(loginUrl, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const body = JSON.parse(res.body);
    return { token: body.token };
}

// Função auxiliar para gerar UUIDs aleatórios para a chave de idempotência
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// 3. Execução: Disparos concorrentes de transferência
export default function (data) {
    const transferUrl = 'http://localhost:8080/api/v1/transfers';

    // Bob é o recebedor da transferência
    const bobWalletId = 'a316df7d-906f-4005-a3d1-0720c9e4b2c9';

    const payload = JSON.stringify({
        receiverWalletId: bobWalletId,
        amount: 10.00,
        idempotencyKey: generateUUID() // Cada tentativa tem uma chave única
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`
        },
    };

    const res = http.post(transferUrl, payload, params);

    // Validação: A transferência pode dar 200 (sucesso) ou 400 (saldo insuficiente),
    // mas NUNCA pode dar 500 (Deadlock/Erro de servidor)!
    check(res, {
        'Transferência processada com sucesso (200)': (r) => r.status === 200,
        'Sem Deadlock ou Crash no Servidor (Status != 500)': (r) => r.status !== 500,
    });
}