import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Distributed_Rate {

    // Token Bucket class
    static class TokenBucket {

        private int maxTokens;
        private double refillRatePerMillis;
        private AtomicInteger tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, int refillPerHour) {
            this.maxTokens = maxTokens;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();

            long hourMillis = 3600 * 1000;
            this.refillRatePerMillis = (double) refillPerHour / hourMillis;
        }

        // Refill tokens based on time passed
        private synchronized void refill() {

            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;

            int refillTokens = (int) (elapsed * refillRatePerMillis);

            if (refillTokens > 0) {

                int newTokens = Math.min(
                        maxTokens,
                        tokens.get() + refillTokens
                );

                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }

        // Try to consume token
        public synchronized boolean allowRequest() {

            refill();

            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }

            return false;
        }

        public int remainingTokens() {
            return tokens.get();
        }
    }

    // clientId -> token bucket
    private ConcurrentHashMap<String, TokenBucket> clients =
            new ConcurrentHashMap<>();

    private int limitPerHour;

    public Distributed_Rate(int limitPerHour) {
        this.limitPerHour = limitPerHour;
    }

    // Check rate limit
    public String checkRateLimit(String clientId) {

        TokenBucket bucket =
                clients.computeIfAbsent(
                        clientId,
                        id -> new TokenBucket(limitPerHour, limitPerHour)
                );

        if (bucket.allowRequest()) {

            return "Allowed (" + bucket.remainingTokens() + " requests remaining)";
        }

        return "Denied (0 requests remaining, retry later)";
    }

    // Get status
    public String getRateLimitStatus(String clientId) {

        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) {
            return "No usage yet";
        }

        int remaining = bucket.remainingTokens();

        return "{used: " + (limitPerHour - remaining) +
                ", limit: " + limitPerHour +
                ", remaining: " + remaining + "}";
    }

    // Demo
    public static void main(String[] args) {

        Distributed_Rate limiter = new Distributed_Rate(5);

        String client = "abc123";

        for (int i = 0; i < 7; i++) {

            System.out.println(
                    limiter.checkRateLimit(client)
            );
        }

        System.out.println(
                limiter.getRateLimitStatus(client)
        );
    }
}
