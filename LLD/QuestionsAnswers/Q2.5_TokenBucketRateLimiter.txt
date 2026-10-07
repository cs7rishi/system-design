public class Q2.5_TokenBucketRateLimiter {
    private final double refillRatePerMs;
    private final int capacity;
    
    private double availableTokens;
    private long lastRefillTimestamp;

    public Q2.5_TokenBucketRateLimiter(double tokensPerSecond, int capacity) {
        this.refillRatePerMs = tokensPerSecond / 1000.0;
        this.capacity = capacity;
        this.availableTokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (availableTokens >= 1.0) {
            availableTokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedMs = now - lastRefillTimestamp;
        
        if (elapsedMs > 0) {
            double tokensToAdd = elapsedMs * refillRatePerMs;
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }

    public synchronized int getAvailableTokens() {
        refill();
        return (int) availableTokens;
    }
}