package com.murillo.finsecure_api.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final ProxyManager<String> proxyManager;

    public RateLimitService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key, RateLimitType type) {
        return proxyManager.builder().build("ratelimit:" + key, () -> createBucketConfiguration(type));
    }

    private BucketConfiguration createBucketConfiguration(RateLimitType type) {
        Bandwidth limit = switch (type) {
            case AUTH_LOGIN -> Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(1))
                    .build();

            case TRANSFER -> Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(1))
                    .build();

            case DEFAULT -> Bandwidth.builder()
                    .capacity(60)
                    .refillIntervally(60, Duration.ofMinutes(1))
                    .build();
        };

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    public enum RateLimitType {
        AUTH_LOGIN,
        TRANSFER,
        DEFAULT
    }
}