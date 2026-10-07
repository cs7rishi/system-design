import java.util.concurrent.Callable;

public class Q2.6_RetryPolicy {
    private final int maxRetries;
    private final long initialDelayMs;
    private final long maxDelayMs;

    public Q2.6_RetryPolicy(int maxRetries, long initialDelayMs, long maxDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return operation.call();
            } catch (Exception e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw e; // Max retries exceeded
                }
                
                long delay = getDelay(attempt);
                System.out.println("Operation failed. Retrying in " + delay + "ms (Attempt " + attempt + ")");
                Thread.sleep(delay);
            }
        }
    }

    private long getDelay(int attempt) {
        // Exponential backoff: delay = initialDelay * 2^(attempt - 1)
        long delay = initialDelayMs * (1L << (attempt - 1));
        // Add minor jitter to prevent thundering herd
        long jitter = (long) (Math.random() * 100); 
        return Math.min(delay + jitter, maxDelayMs);
    }
}