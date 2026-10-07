ROUND 1: DSA-Focused Questions
Q1.1: Distributed Lock Manager with Fairness
Category: Concurrency + Priority Queue

Real-World Context (Hevo Platform):
Hevo has multiple worker nodes processing pipelines across thousands of customers. When a pipeline needs exclusive access to a resource (like updating a checkpoint in PostgreSQL or syncing to a Snowflake destination), distributed locks prevent race conditions and ensure only one worker processes that pipeline at a time. Fairness guarantees prevent pipeline starvation when hundreds of pipelines compete for limited destination connections.

Problem Statement:
Design a lock manager for distributed systems where:

Locks can have timeouts (auto-release after T seconds)

When multiple threads wait for a lock, prioritize the longest waiting thread (fairness)

Support try-lock with timeout

Requirements:
Implement LockManager class:

boolean lock(String resourceId, String ownerId, int timeoutSeconds) - Acquire lock

boolean tryLock(String resourceId, String ownerId, int waitTimeMs, int lockTimeoutSeconds) - Try acquiring with wait limit

void unlock(String resourceId, String ownerId) - Release lock

Background thread to auto-release expired locks