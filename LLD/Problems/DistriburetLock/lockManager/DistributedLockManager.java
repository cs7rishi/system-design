package DistriburetLock.lockManager;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class DistributedLockManager {

    // 1. ConcurrentHashMap for lock state storage
    private final ConcurrentHashMap<String, LockState> activeLocks = new ConcurrentHashMap<>();

    // 2. Min-heap for timeout tracking (ordered by expiration time)
    private final PriorityBlockingQueue<LockState> timeoutHeap =
            new PriorityBlockingQueue<>(11, Comparator.comparingLong(l -> l.expirationTimeMs));

    // 3. Priority queues for fairness (FIFO order, mapped per resource)
    private final ConcurrentHashMap<String, PriorityQueue<WaitNode>> waitQueues = new ConcurrentHashMap<>();

    // 4. Coordination lock (fine-grained locking per resource to maximize concurrent throughput)
    private final ConcurrentHashMap<String, ReentrantLock> resourceLocks = new ConcurrentHashMap<>();

    // 5. Scheduled executor for background cleanup (prevents deadlocks)
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread t = new Thread(runnable, "Lock-Cleanup-Thread");
        t.setDaemon(true);
        return t;
    });

    public DistributedLockManager() {
        // Run cleanup every 500ms
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredLocks, 500, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Acquires the lock, blocking until available.
     */
    public boolean lock(String resourceId, String ownerId, int timeoutSeconds) {
        ReentrantLock mutex = getMutex(resourceId);
        mutex.lock();
        try {
            if (!activeLocks.containsKey(resourceId)) {
                grantLock(resourceId, ownerId, timeoutSeconds);
                return true;
            }

            // Lock is busy: enqueue in Priority Queue for FIFO fairness
            Condition condition = mutex.newCondition();
            WaitNode waitNode = new WaitNode(ownerId, System.currentTimeMillis(), condition);
            waitQueues.computeIfAbsent(resourceId, k -> new PriorityQueue<>()).offer(waitNode);

            try {
                // Block until the lock is handed off to this specific thread
                while (activeLocks.containsKey(resourceId) && !waitNode.isGranted) {
                    condition.await();
                }

                if (waitNode.isGranted) {
                    grantLock(resourceId, ownerId, timeoutSeconds);
                    return true;
                }
            } catch (InterruptedException e) {
                waitQueues.get(resourceId).remove(waitNode);
                Thread.currentThread().interrupt();
                return false; // Fail gracefully if thread is interrupted
            }
            return false;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Tries to acquire the lock, waiting up to waitTimeMs before giving up.
     */
    public boolean tryLock(String resourceId, String ownerId, int waitTimeMs, int lockTimeoutSeconds) {
        ReentrantLock mutex = getMutex(resourceId);
        mutex.lock();
        try {
            if (!activeLocks.containsKey(resourceId)) {
                grantLock(resourceId, ownerId, lockTimeoutSeconds);
                return true;
            }

            Condition condition = mutex.newCondition();
            WaitNode waitNode = new WaitNode(ownerId, System.currentTimeMillis(), condition);
            waitQueues.computeIfAbsent(resourceId, k -> new PriorityQueue<>()).offer(waitNode);

            long nanosToWait = TimeUnit.MILLISECONDS.toNanos(waitTimeMs);
            try {
                while (activeLocks.containsKey(resourceId) && !waitNode.isGranted) {
                    if (nanosToWait <= 0L) {
                        waitQueues.get(resourceId).remove(waitNode); // Remove from queue to prevent starvation memory leak
                        return false; // Timed out waiting
                    }
                    nanosToWait = condition.awaitNanos(nanosToWait);
                }

                if (waitNode.isGranted) {
                    grantLock(resourceId, ownerId, lockTimeoutSeconds);
                    return true;
                }
            } catch (InterruptedException e) {
                waitQueues.get(resourceId).remove(waitNode);
                Thread.currentThread().interrupt();
                return false;
            }
            return false;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Releases the lock and explicitly hands it to the next waiting thread.
     */
    public void unlock(String resourceId, String ownerId) {
        ReentrantLock mutex = getMutex(resourceId);
        mutex.lock();
        try {
            LockState currentLock = activeLocks.get(resourceId);
            if (currentLock != null && currentLock.ownerId.equals(ownerId)) {
                activeLocks.remove(resourceId);

                // Note: We intentionally do NOT remove the lock from the timeoutHeap here. 
                // PriorityBlockingQueue.remove() is O(N). We let the cleanup thread lazily discard it in O(1) time.

                handoffToNextInQueue(resourceId);
            }
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Background thread process to auto-release expired locks.
     */
    private void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();

        while (!timeoutHeap.isEmpty() && timeoutHeap.peek().expirationTimeMs <= now) {
            LockState expiredLock = timeoutHeap.poll();
            if (expiredLock == null) break;

            ReentrantLock mutex = getMutex(expiredLock.resourceId);
            mutex.lock();
            try {
                LockState currentLock = activeLocks.get(expiredLock.resourceId);

                // Verify the lock hasn't been explicitly unlocked or renewed by a different owner
                if (currentLock != null
                        && currentLock.ownerId.equals(expiredLock.ownerId)
                        && currentLock.expirationTimeMs <= now) {

                    activeLocks.remove(expiredLock.resourceId);
                    handoffToNextInQueue(expiredLock.resourceId);
                }
            } finally {
                mutex.unlock();
            }
        }
    }

    // --- Helper Methods & Internal Data Structures ---

    private void handoffToNextInQueue(String resourceId) {
        PriorityQueue<WaitNode> pq = waitQueues.get(resourceId);
        if (pq != null && !pq.isEmpty()) {
            WaitNode nextInLine = pq.poll();
            nextInLine.isGranted = true; // Handoff state
            nextInLine.condition.signal(); // Wake up the specific thread
        }
    }

    private void grantLock(String resourceId, String ownerId, int timeoutSeconds) {
        long expirationTimeMs = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        LockState newLock = new LockState(resourceId, ownerId, expirationTimeMs);
        activeLocks.put(resourceId, newLock);
        timeoutHeap.offer(newLock); // Add to min-heap for O(1) expiration tracking
    }

    private ReentrantLock getMutex(String resourceId) {
        return resourceLocks.computeIfAbsent(resourceId, k -> new ReentrantLock());
    }

    private static class LockState {
        final String resourceId;
        final String ownerId;
        final long expirationTimeMs;

        LockState(String resourceId, String ownerId, long expirationTimeMs) {
            this.resourceId = resourceId;
            this.ownerId = ownerId;
            this.expirationTimeMs = expirationTimeMs;
        }
    }

    private static class WaitNode implements Comparable<WaitNode> {
        final String ownerId;
        final long timestamp;
        final Condition condition;
        boolean isGranted = false;

        WaitNode(String ownerId, long timestamp, Condition condition) {
            this.ownerId = ownerId;
            this.timestamp = timestamp;
            this.condition = condition;
        }

        @Override
        public int compareTo(WaitNode other) {
            return Long.compare(this.timestamp, other.timestamp); // Ensures FIFO priority
        }
    }
}