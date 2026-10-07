# LLD Practice — Problem Hints

These entries match the names and order in `question.md`. Patterns are candidates to evaluate, not requirements to force into every solution. Clarify scope before designing classes.

## 1. Parking Lot

- **Focus:** Multiple floors, vehicle types, spot allocation, entry/exit gates, tickets, and pricing.
- **Possible patterns:** Strategy for spot allocation and fee calculation; Factory if creating different vehicle or spot types needs encapsulation.
- **Design hints:** Separate parking inventory, allocation, tickets, and billing. Model vehicle-to-spot compatibility explicitly.
- **Follow-ups:** Two gates competing for the last spot; lost tickets; full capacity; extensible pricing rules; preventing duplicate checkout.

## 2. Vending Machine

- **Focus:** Product selection, inventory, payments, dispensing, change calculation, and cancellation.
- **Possible patterns:** State for legal transitions; Strategy for payment methods or change calculation when variation is needed.
- **Design hints:** Define permitted actions in each state. Distinguish product metadata from stock and track money inserted for the current transaction.
- **Follow-ups:** Insufficient money or change; sold-out products; dispensing failure; refunds; concurrent purchases.

## 3. Tic-Tac-Toe

- **Focus:** Board modeling, players, turns, move validation, winning rules, and draw detection.
- **Possible patterns:** Strategy for interchangeable winning rules or human/bot move selection.
- **Design hints:** Separate board state from game orchestration and rule evaluation. Start with a working two-player game.
- **Follow-ups:** N×N boards; K-in-a-row wins; occupied cells; moves after the game ends; undo support; efficient win detection.

## 4. Logging Framework

- **Focus:** Log levels, formatters, multiple output destinations, asynchronous logging, and thread safety.
- **Possible patterns:** Strategy for formatting and output behavior; Composite if treating multiple destinations as one destination is useful.
- **Design hints:** Separate log events, filtering, formatting, and appenders. For asynchronous mode, define the queue and worker lifecycle.
- **Follow-ups:** Queue saturation; slow or failed destinations; ordering; flushing on shutdown; runtime configuration changes.

## 5. Elevator System

- **Focus:** Internal/external requests, elevator selection, scheduling, movement, and door states.
- **Possible patterns:** Strategy for dispatch and scheduling; State for elevator or door behavior.
- **Design hints:** Separate the building dispatcher from each elevator controller. Distinguish hall calls from destination requests inside a car.
- **Follow-ups:** Multiple elevators; requests arriving during movement; starvation; capacity limits; maintenance; emergency behavior.

## 6. Splitwise / Expense Sharing

- **Focus:** Users, groups, expenses, equal/exact/percentage splits, balances, and settlements.
- **Possible patterns:** Strategy for split calculation; Factory if selecting split implementations becomes complex.
- **Design hints:** Validate that shares sum to the expense. Use decimal money or integer minor units with an explicit rounding policy. Keep expense history separate from derived balances.
- **Follow-ups:** Rounding leftovers; expense edits/deletions; duplicate submissions; concurrent updates; debt simplification as an optional extension.

## 7. Movie Ticket Booking — BookMyShow

- **Focus:** Movies, theatres, screens, shows, seats, temporary holds, payments, and cancellation.
- **Possible patterns:** State for booking lifecycle; Strategy for pricing or payment providers when needed.
- **Design hints:** Track availability per show and seat. Define atomic seat acquisition, hold ownership, expiration, and confirmation rules.
- **Follow-ups:** Two users selecting the same seat; all-or-nothing multi-seat holds; payment succeeding after expiry; duplicate payment callbacks; refunds.

## 8. Cache — LRU/LFU with TTL

- **Focus:** Efficient get/put operations, bounded capacity, eviction policies, expiration, and concurrency.
- **Possible patterns:** Strategy for interchangeable eviction policies.
- **Design hints:** Start with LRU using a hash map and doubly linked list. Treat expiration and capacity eviction as separate concerns. Define whether TTL resets on reads or writes.
- **Follow-ups:** LFU tie-breaking; lazy versus proactive expiry; atomic updates across data structures; injected clock for testing; cache stampedes if loading is supported.

## 9. Rate Limiter

- **Focus:** Per-user/API limits, token bucket or sliding window policies, time handling, and atomic updates.
- **Possible patterns:** Strategy for interchangeable limiting algorithms.
- **Design hints:** Begin with an in-memory component exposing an allow/reject operation. Define the rate-limit key, burst allowance, time source, and cleanup of inactive keys.
- **Follow-ups:** Concurrent requests; window-boundary bursts; refill precision; retry-after calculation; per-tenant configuration. Treat distributed enforcement as a separate extension.

## 10. In-process Pub/Sub System

- **Focus:** Topics, publishers, subscribers, asynchronous delivery, ordering, slow consumers, and retries.
- **Possible patterns:** Observer for subscriptions; producer-consumer queues for asynchronous dispatch; Strategy for retry policies if configurable.
- **Design hints:** Separate topic/subscription management from delivery. Define whether each subscriber receives its own copy and what ordering guarantees apply.
- **Follow-ups:** Subscriber exceptions; bounded queues and backpressure; unsubscribe during delivery; duplicate deliveries after retries; graceful shutdown. Keep the initial design within one process.

## How we will work through each problem

1. Clarify requirements and scope.
2. Identify classes, responsibilities, and invariants.
3. Define interfaces and key interactions.
4. Implement the core flow in Java.
5. Handle edge cases and concurrency.
6. Discuss extensions and interview follow-up questions.

## Keeping both files current

Whenever we add a practice problem beyond this initial list, append its name to `question.md` and add a matching hints section here. Preserve matching names, numbering, and order. Provide updated versions of both files; if an update cannot be completed, remind the user that both need updating.
