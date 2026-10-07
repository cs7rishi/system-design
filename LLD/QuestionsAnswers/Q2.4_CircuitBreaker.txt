import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Q2.4_CircuitBreaker {
    
    public enum CircuitState { CLOSED, OPEN, HALF_OPEN }
    
    public static class CircuitOpenException extends RuntimeException {
        public CircuitOpenException(String message) { super(message); }
    }

    private final int failureThreshold;
    private final long cooldownMs;
    
    private final AtomicReference<CircuitState> state;
    private final AtomicInteger failureCount;
    private volatile long lastFailureTime;

    public Q2.4_CircuitBreaker(int failureThreshold, long cooldownMs) {
        this.failureThreshold = failureThreshold;
        this.cooldownMs = cooldownMs;
        this.state = new AtomicReference<>(CircuitState.CLOSED);
        this.failureCount = new AtomicInteger(0);
        this.lastFailureTime = 0;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        if (!allowRequest()) {
            throw new CircuitOpenException("Circuit is OPEN");
        }

        try {
            T result = operation.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private boolean allowRequest() {
        CircuitState currentState = state.get();
        if (currentState == CircuitState.CLOSED) {
            return true;
        }
        
        if (currentState == CircuitState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= cooldownMs) {
                return state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN);
            }
            return false;
        }
        
        // HALF_OPEN: allow one probe request. 
        // Note: Real implementations might use a semaphore for more than 1 concurrent probe.
        return true; 
    }

    private void onSuccess() {
        failureCount.set(0);
        state.set(CircuitState.CLOSED);
    }

    private void onFailure() {
        lastFailureTime = System.currentTimeMillis();
        if (state.get() == CircuitState.HALF_OPEN) {
            state.set(CircuitState.OPEN);
        } else if (failureCount.incrementAndGet() >= failureThreshold) {
            state.set(CircuitState.OPEN);
        }
    }

    public CircuitState getState() {
        return state.get();
    }

    public void reset() {
        state.set(CircuitState.CLOSED);
        failureCount.set(0);
    }
}