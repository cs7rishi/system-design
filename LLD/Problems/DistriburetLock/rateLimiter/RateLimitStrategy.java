package DistriburetLock.rateLimiter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

interface RateLimitStrategy {
    boolean allowRequests(String clientId);
}

class FixedWindow implements RateLimitStrategy{
    private final int limit;
    private final long windowSizeMs;
    private final Map<String, WindowState> clients = new ConcurrentHashMap<>();

    public FixedWindow(int limit, long windowSizeMs) {
        this.limit = limit;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public boolean allowRequests(String clientId) {
        long now = System.currentTimeMillis();
        long currentWindowStart = (now / windowSizeMs) * windowSizeMs;
        WindowState state = clients.computeIfAbsent(clientId, k -> new WindowState());
        synchronized (state){
            if(state.windowStart != currentWindowStart){
                state.windowStart = currentWindowStart;
                state.count = 0;
            }

            if(state.count < limit){
                state.count++;
                return true;
            }
            return false;
        }
    }

    private static class WindowState{
        long windowStart = 0;
        int count = 0;
    }
}

class SlidingWindowLog implements RateLimitStrategy{
    private final int limit;
    private final long windowSizeMs;
    private final Map<String, Deque<Long>> clients = new ConcurrentHashMap<>();

    public SlidingWindowLog(int limit, long windowSizeMs) {
        this.limit = limit;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public boolean allowRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> log = clients.computeIfAbsent(clientId, k -> new LinkedList<>());

        synchronized (log){
            while(!log.isEmpty() && log.peekFirst() <= now - windowSizeMs){
                log.pollFirst();
            }

            if(log.size() < limit){
                log.addLast(now);
                return true;
            }
            return false;
        }
    }
}

class TokenBucket implements RateLimitStrategy{

    private final int capacity;
    private final double refillRatePerSec;
    private final Map<String, BucketState> clients
            = new ConcurrentHashMap<>();

    public TokenBucket(int capacity, double refillRatePerSec) {
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;
    }

    @Override
    public boolean allowRequests(String clientId) {
        long now = System.currentTimeMillis();
        BucketState state
                = clients.computeIfAbsent(clientId, k -> new BucketState(capacity,now));

        synchronized (state){
            long timePassedMs = now - state.lastRefillTime;

            double newTokens = (timePassedMs/1000) * refillRatePerSec;
            state.tokens = Math.min(capacity, state.tokens + newTokens );
            state.lastRefillTime = now;

            if(state.tokens >= 1.0){
                state.tokens -= 1.0;
                return true;
            }
        }
        return false;
    }

    private static class BucketState{
        double tokens;
        long lastRefillTime;

        public BucketState(double tokens, long lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}