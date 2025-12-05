package com.example.chatchannels.server.ratelimit;

import com.example.chatchannels.config.ChannelConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiterManager {

    private static final RateLimiterManager INSTANCE = new RateLimiterManager();

    private final Map<UUID, RateLimiter> limiters = new ConcurrentHashMap<>();

    private volatile int capacity = 5;
    private volatile int perSeconds = 3;

    private RateLimiterManager() {
    }

    public static RateLimiterManager getInstance() {
        return INSTANCE;
    }

    public void configure(ChannelConfig config) {
        if (config != null && config.rateLimit != null) {
            this.capacity = Math.max(1, config.rateLimit.messages);
            this.perSeconds = Math.max(1, config.rateLimit.perSeconds);
        }
        limiters.clear();
    }

    public boolean tryConsume(UUID playerId) {
        RateLimiter limiter = limiters.computeIfAbsent(playerId, id -> new RateLimiter(capacity, perSeconds));
        return limiter.tryConsume();
    }
}
