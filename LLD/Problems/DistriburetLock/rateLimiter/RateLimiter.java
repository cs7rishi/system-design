package DistriburetLock.rateLimiter;


public class RateLimiter {
    private final RateLimitStrategy strategy;

    public RateLimiter(RateLimitStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean allowRequests(String clientId){
        return strategy.allowRequests(clientId);
    }
}


