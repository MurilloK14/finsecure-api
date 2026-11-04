package com.murillo.finsecure_api.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, Bucket> bucket = new ConcurrentHashMap<>();


    public Bucket resolveBucket(String key, RateLimitType type) {
        return bucket.computeIfAbsent(key, k -> createNewBucket(type));
    }

    private Bucket createNewBucket(RateLimitType type) {
        return switch (type){
            case AUTH_LOGIN -> Bucket.builder()
                    .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build();

            case TRANSFER -> Bucket.builder()
                    .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                    .build();
            case DEFAULT -> Bucket.builder()
                    .addLimit(Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1))))
                    .build();
        };
    }

    public enum RateLimitType {
        AUTH_LOGIN,
        TRANSFER,
        DEFAULT
    }
}
