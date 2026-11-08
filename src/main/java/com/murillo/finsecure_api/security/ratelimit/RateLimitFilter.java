package com.murillo.finsecure_api.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key;
        RateLimitService.RateLimitType type;

        if (path.equals("/api/v1/auth/login")) {
            // No login: limitamos por IP (proteção contra brute-force de senhas)
            key = "LOGIN:" + getClientIp(request);
            type = RateLimitService.RateLimitType.AUTH_LOGIN;
        } else if (path.startsWith("/api/v1/transfers")) {
            // Em transferências: limitamos pelo ID/E-mail do usuário autenticado no JWT
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String identity = (auth != null && auth.isAuthenticated()) ? auth.getName() : getClientIp(request);
            key = "TRANSFER:" + identity;
            type = RateLimitService.RateLimitType.TRANSFER;
        } else {
            key = "DEFAULT:" + getClientIp(request);
            type = RateLimitService.RateLimitType.DEFAULT;
        }
        // 2. Resolve o balde daquele cliente
        Bucket bucket = rateLimitService.resolveBucket(key, type);
        // 3. Tenta consumir 1 ficha do balde
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            // Sucesso! Anexa no cabeçalho quantas requisições ainda restam
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Falha! O balde esvaziou -> Retorna HTTP 429 Too Many Requests
            long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(waitForRefillSeconds));
            String jsonResponse = String.format("""
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Limite de requisições excedido. Tente novamente em %d segundos.",
                      "path": "%s"
                    }
                    """, waitForRefillSeconds, path);
            response.getWriter().write(jsonResponse);
        }
    }

    // Método utilitário para capturar o IP real mesmo atrás de Proxies/Load Balancers
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
