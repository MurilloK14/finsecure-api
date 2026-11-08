import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuração do teste: 1 usuário virtual fazendo 15 disparos em sequência
export const options = {
  vus: 1,
  iterations: 15,
};

export default function (data) {
  const url = 'http://localhost:8080/api/v1/auth/login';

  const payload = JSON.stringify({
    email: 'stress-test@finsecure.com',
    password: 'SenhaInvalida123!'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      // Simula um IP específico por rodada de teste
      'X-Forwarded-For': '192.168.100.55'
    },
  };

  const res = http.post(url, payload, params);

  // Validações da blindagem de AppSec
    // Se for uma das 5 primeiras tentativas (0 a 4), tem que passar.
    // Da 6ª em diante (5 a 14), TEM que ser bloqueado com 429!
    if (__ITER < 5) {
        check(res, {
            'Tentativas 1 a 5 -> Permitidas (Status != 429)': (r) => r.status !== 429,
        });
    } else {
        check(res, {
            'Tentativas 6 a 15 -> Bloqueadas com sucesso (HTTP 429)': (r) => r.status === 429,
        });
    }

  sleep(0.05); // Disparo ultrarrápido (50ms)
}
