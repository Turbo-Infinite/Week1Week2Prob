import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TokenBucket {
    private final int maxTokens;
    private final int refillRate; // tokens per hour
    private AtomicInteger tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(maxTokens);
        this.lastRefillTime = System.currentTimeMillis();
    }

    // Refill tokens every hour
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;

        if (elapsed >= 3600_000) { // 1 hour in ms
            tokens.set(maxTokens);
            lastRefillTime = now;
        }
    }

    // Try to consume a token
    public synchronized boolean allowRequest() {
        refill();
        if (tokens.get() > 0) {
            tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    public int getRemainingTokens() {
        refill();
        return tokens.get();
    }

    public long getResetTime() {
        return lastRefillTime + 3600_000;
    }
}

class RateLimiter {
    private HashMap<String, TokenBucket> clientBuckets = new HashMap<>();
    private final int MAX_TOKENS = 1000;
    private final int REFILL_RATE = 1000; // per hour

    // Check rate limit
    public synchronized String checkRateLimit(String clientId) {
        clientBuckets.putIfAbsent(clientId, new TokenBucket(MAX_TOKENS, REFILL_RATE));
        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retryAfter = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    // Get status
    public synchronized String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) return "Client not found";

        int used = MAX_TOKENS - bucket.getRemainingTokens();
        return "{used: " + used + ", limit: " + MAX_TOKENS + ", reset: " + bucket.getResetTime() + "}";
    }

    // Demo
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        for (int i = 0; i < 1002; i++) {
            System.out.println("Request " + (i+1) + ": " + limiter.checkRateLimit("abc123"));
        }

        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}
