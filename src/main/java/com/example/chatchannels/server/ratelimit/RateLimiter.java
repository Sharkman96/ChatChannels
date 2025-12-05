package com.example.chatchannels.server.ratelimit;

public class RateLimiter {

    private final int capacity;
    private final int refillSeconds;

    private double tokens;
    private long lastRefillTimeMillis;

    public RateLimiter(int capacity, int refillSeconds) {
        this.capacity = capacity;
        this.refillSeconds = Math.max(1, refillSeconds);
        this.tokens = capacity;
        this.lastRefillTimeMillis = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimeMillis;
        if (elapsed <= 0) {
            return;
        }
        double tokensPerMs = (double) capacity / (refillSeconds * 1000.0);
        tokens = Math.min(capacity, tokens + elapsed * tokensPerMs);
        lastRefillTimeMillis = now;
    }
}
