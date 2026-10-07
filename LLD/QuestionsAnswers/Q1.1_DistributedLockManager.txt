import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.PriorityQueue;

public class Q1.1_DistributedLockManager {

    private static class LockRequest implements Comparable<LockRequest> {
        final String ownerId;
        final long requestTime;
        final long timeoutMs;
        final CountDownLatch latch;

        LockRequest(String ownerId, long timeoutMs) {
            this.ownerId = ownerId;
            this.requestTime = System.currentTimeMillis();
            this.timeoutMs = timeoutMs;
            this.latch = new CountDownLatch(1);
        }

        @Override
        public int compareTo(LockRequest other) {
            return Long.compare(this.requestTime, other.requestTime);
        }
    }

    private static class LockState {
        String currentOwner = null;
        long expirationTime = 0;
        final PriorityQueue<LockRequest> waitQueue = new PriorityQueue<>();
        final ReentrantLock syncLock = new ReentrantLock();
    }

    private final ConcurrentHashMap<String, LockState> locks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public Q1.1_DistributedLockManager() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredLocks, 1, 1, TimeUnit.SECONDS);
    }

    public boolean lock(String resourceId, String ownerId, int timeoutSeconds) {
        return tryLock(resourceId, ownerId, Integer.MAX_VALUE, timeoutSeconds);
    }

    public boolean tryLock(String resourceId, String ownerId, int waitTimeMs, int lockTimeoutSeconds) {
        LockState state = locks.computeIfAbsent(resourceId, k -> new LockState());
        LockRequest request = new LockRequest(ownerId, lockTimeoutSeconds * 1000L);

        state.syncLock.lock();
        try {
            if (state.currentOwner == null) {
                grantLock(state, ownerId, request.timeoutMs);
                return true;
            }
            state.waitQueue.offer(request);
        } finally {
            state.syncLock.unlock();
        }

        try {
            if (request.latch.await(waitTimeMs, TimeUnit.MILLISECONDS)) {
                return true;
            } else {
                state.syncLock.lock();
                try {
                    state.waitQueue.remove(request);
                } finally {
                    state.syncLock.unlock();
                }
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String resourceId, String ownerId) {
        LockState state = locks.get(resourceId);
        if (state == null) return;

        state.syncLock.lock();
        try {
            if (ownerId.equals(state.currentOwner)) {
                state.currentOwner = null;
                LockRequest next = state.waitQueue.poll();
                if (next != null) {
                    grantLock(state, next.ownerId, next.timeoutMs);
                    next.latch.countDown();
                }
            }
        } finally {
            state.syncLock.unlock();
        }
    }

    private void grantLock(LockState state, String ownerId, long timeoutMs) {
        state.currentOwner = ownerId;
        state.expirationTime = System.currentTimeMillis() + timeoutMs;
    }

    private void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();
        for (String resourceId : locks.keySet()) {
            LockState state = locks.get(resourceId);
            state.syncLock.lock();
            try {
                if (state.currentOwner != null && state.expirationTime < now) {
                    String expiredOwner = state.currentOwner;
                    state.syncLock.unlock(); // Prevent deadlock before calling unlock
                    unlock(resourceId, expiredOwner);
                    state.syncLock.lock(); // Reacquire for consistency
                }
            } finally {
                state.syncLock.unlock();
            }
        }
    }
}