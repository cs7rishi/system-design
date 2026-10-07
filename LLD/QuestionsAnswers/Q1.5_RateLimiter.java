import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Q1.5_RateLimiter {
    private final int maxRequests;
    private final long windowSizeMs;
    private final ConcurrentHashMap<String, WindowData> clientWindows = new ConcurrentHashMap<>();

    private static class WindowData {
        long currentWindowStart;
        AtomicInteger previousCount = new AtomicInteger(0);
        AtomicInteger currentCount = new AtomicInteger(0);
        
        WindowData(long startTime) { this.currentWindowStart = startTime; }
    }

    public Q1.5_RateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSeconds * 1000L;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        long currentWindowStart = (now / windowSizeMs) * windowSizeMs;

        WindowData data = clientWindows.compute(clientId, (key, existingData) -> {
            if (existingData == null) return new WindowData(currentWindowStart);
            if (existingData.currentWindowStart != currentWindowStart) {
                // Slide window forward
                long elapsedWindows = (currentWindowStart - existingData.currentWindowStart) / windowSizeMs;
                existingData.previousCount.set(elapsedWindows == 1 ? existingData.currentCount.get() : 0);
                existingData.currentCount.set(0);
                existingData.currentWindowStart = currentWindowStart;
            }
            return existingData;
        });

        double overlapPercentage = 1.0 - ((double)(now - currentWindowStart) / windowSizeMs);
        int estimatedTotal = (int)(data.previousCount.get() * overlapPercentage) + data.currentCount.get() + 1;

        if (estimatedTotal <= maxRequests) {
            data.currentCount.incrementAndGet();
            return true;
        }
        return false;
    }
}