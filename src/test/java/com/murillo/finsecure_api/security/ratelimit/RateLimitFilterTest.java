package com.murillo.finsecure_api.security.ratelimit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    private RateLimitFilter rateLimitFilter;

    @Mock
    private FilterChain filterChain;


    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService);
    }

    @Test
    @DisplayName("Deve permitir as 5 primeiras tentativas de login do mesmo IP")
    void doFilter_DevePermitirAteCincoTentativasDeLogin() throws ServletException, IOException {
        String clientIp = "192.168.1.50";

        Bucket testBucket = Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();
        Mockito.when(rateLimitService.resolveBucket(any(), any())).thenReturn(testBucket);
        for (int i = 1; i<=5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/auth/login");
            request.setRemoteAddr(clientIp);

            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitFilter.doFilter(request, response, filterChain);

            Assertions.assertThat(response.getStatus()).isEqualTo(200);
            Assertions.assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNotNull();

        }
        verify(filterChain, times(5)).doFilter(any(), any());
     }

     @Test
    @DisplayName(("Deve bloquear com HTTP 429 na 6º tentativa de login no mesmo IP"))
    void doFilter_DeveBloquearComHttp429_QuandoExcederLimmiteDeCincoTentativasDeLogin() throws ServletException, IOException{
        String clientIp = "192.168.1.99";

         Bucket testBucket = Bucket.builder()
                 .addLimit(Bandwidth.builder()
                         .capacity(5)
                         .refillIntervally(5, Duration.ofMinutes(1))
                         .build())
                 .build();
         Mockito.when(rateLimitService.resolveBucket(any(), any())).thenReturn(testBucket);

         for(int i = 1; i<= 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/auth/login");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitFilter.doFilter(request,response,filterChain);
         }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest();
        blockedRequest.setRequestURI("/api/v1/auth/login");
        blockedRequest.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitFilter.doFilter(blockedRequest, blockedResponse, filterChain);

        Assertions.assertThat(blockedResponse.getStatus()).isEqualTo(429);
        Assertions.assertThat(blockedResponse.getHeader("Retry-After")).isNotNull();
        Assertions.assertThat(blockedResponse.getContentAsString()).contains("Too Many Requests");
        Assertions.assertThat(blockedResponse.getContentAsString()).contains("Limite de requisições excedido");
            verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve ignorar rotas do Swagger sem consumir fichas")
    void doFilter_DeveIgnorarRotasDoSwagger() throws ServletException, IOException{
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitFilter.doFilter(request, response,filterChain);

        Assertions.assertThat(response.getStatus()).isEqualTo(200);
        Assertions.assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
