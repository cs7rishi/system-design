# Observer Pattern — Revision Notes

## 1. Core Idea

The **Observer Pattern** is a behavioral design pattern used when **one object changes state or emits an event and multiple other objects need to be notified automatically**.

### Mental Model

```text
             Subject / Publisher
                    |
             state changes
                    |
       +------------+------------+
       |            |            |
       v            v            v
   Observer 1   Observer 2   Observer 3
```

Typical examples:

```text
Order status changes
    |
    +--> Email notification
    +--> SMS notification
    +--> Analytics update

YouTube channel uploads video
    |
    +--> Subscriber A
    +--> Subscriber B
    +--> Subscriber C
```

### Interview Definition

> Observer defines a **one-to-many dependency** so that when the Subject changes state or emits an event, all registered Observers are notified automatically.

---

## 2. Problem Without Observer

Suppose an `OrderService` directly handles all side effects:

```java
public class OrderService {

    public void updateOrderStatus(String status) {
        System.out.println("Order status updated to " + status);

        sendEmail(status);
        sendSms(status);
        updateAnalytics(status);
    }

    private void sendEmail(String status) {
        System.out.println("Email sent");
    }

    private void sendSms(String status) {
        System.out.println("SMS sent");
    }

    private void updateAnalytics(String status) {
        System.out.println("Analytics updated");
    }
}
```

This creates tight coupling:

```text
OrderService
    |
    +-- Email logic
    +-- SMS logic
    +-- Analytics logic
    +-- Slack logic
    +-- Webhook logic
    +-- Audit logic
```

Every new reaction requires modifying the Subject-like class.

Problems:

- large class
- tight coupling
- poor separation of concerns
- difficult testing
- repeated modification when new listeners are added

---

## 3. Observer Pattern Solution

Separate responsibilities:

```text
Subject
-> owns the state/event

Observer
-> reacts to the change/event
```

The Subject does not care what the Observer does internally.

It only knows:

```java
observer.update(...);
```

---

## 4. Main Roles

### Subject / Publisher

The object whose state changes or which produces events.

Responsibilities:

- stores registered observers
- allows subscribe/register
- allows unsubscribe/remove
- notifies observers

### Observer

Common contract for all listeners.

```java
public interface OrderObserver {
    void update(String status);
}
```

### Concrete Observer

Implements actual reaction logic.

Examples:

- `EmailObserver`
- `SmsObserver`
- `AnalyticsObserver`

### Client

Registers observers with the Subject and triggers operations.

---

## 5. Architecture Diagram

```text
                 Subject
        +----------------------+
        | List<Observer>       |
        +----------------------+
        | addObserver()        |
        | removeObserver()     |
        | notifyObservers()    |
        +----------+-----------+
                   |
                   | HAS-A many
                   v
              Observer
            <<interface>>
          +-------------+
          | update()    |
          +------+------+
                 ^
                 |
       +---------+---------+
       |                   |
       v                   v
ConcreteObserver A   ConcreteObserver B
```

Important relationships:

```text
Subject HAS-A List<Observer>

ConcreteObserver IS-A Observer
```

---

## 6. Java 8 Implementation

### OrderObserver.java

```java
package Behavioral.Observer;

public interface OrderObserver {

    void update(String status);
}
```

### EmailObserver.java

```java
package Behavioral.Observer;

public class EmailObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "Email notification: order status = " + status
        );
    }
}
```

### SmsObserver.java

```java
package Behavioral.Observer;

public class SmsObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "SMS notification: order status = " + status
        );
    }
}
```

### AnalyticsObserver.java

```java
package Behavioral.Observer;

public class AnalyticsObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "Analytics updated for status = " + status
        );
    }
}
```

### Order.java

```java
package Behavioral.Observer;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private String status;

    private final List<OrderObserver> observers =
            new ArrayList<OrderObserver>();

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.update(status);
        }
    }
}
```

### Client.java

```java
package Behavioral.Observer;

public class Client {

    public static void main(String[] args) {

        Order order = new Order();

        OrderObserver emailObserver =
                new EmailObserver();

        OrderObserver smsObserver =
                new SmsObserver();

        OrderObserver analyticsObserver =
                new AnalyticsObserver();

        order.addObserver(emailObserver);
        order.addObserver(smsObserver);
        order.addObserver(analyticsObserver);

        order.setStatus("SHIPPED");
    }
}
```

Output:

```text
Email notification: order status = SHIPPED
SMS notification: order status = SHIPPED
Analytics updated for status = SHIPPED
```

---

## 7. Execution Flow

When this runs:

```java
order.setStatus("SHIPPED");
```

flow becomes:

```text
setStatus("SHIPPED")
        |
        v
   Order changes
        |
        v
 notifyObservers()
        |
        +----------------------+
        |          |           |
        v          v           v
      Email       SMS      Analytics
```

Inside:

```java
private void notifyObservers() {
    for (OrderObserver observer : observers) {
        observer.update(status);
    }
}
```

The Subject delegates reaction behavior to each registered Observer.

---

## 8. Why This Reduces Coupling

Before:

```text
Order
 |
 +-- knows Email
 +-- knows SMS
 +-- knows Analytics
```

After:

```text
Order
 |
 v
OrderObserver interface
```

The Subject does not need to know whether an Observer:

```text
sends email
writes to Kafka
updates analytics
calls a webhook
writes audit logs
updates a UI
```

It only calls:

```java
observer.update(status);
```

---

## 9. Adding a New Observer

Suppose Slack notifications are required.

```java
package Behavioral.Observer;

public class SlackObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "Slack notification: order status = " + status
        );
    }
}
```

Register it:

```java
order.addObserver(new SlackObserver());
```

The `Order` class does not change.

This supports the **Open/Closed Principle**.

---

## 10. Subscribe and Unsubscribe

Observers can be dynamically registered:

```java
order.addObserver(emailObserver);
```

and removed:

```java
order.removeObserver(emailObserver);
```

Runtime relationship:

```text
Initially:

Order
 |
 +-- Email
 +-- SMS
 +-- Analytics

Later:

Order
 |
 +-- SMS
 +-- Analytics
```

---

## 11. Push Model vs Pull Model

This is a common interview question.

### Push Model

The Subject sends the needed data directly:

```java
observer.update(status);
```

Observer:

```java
void update(String status);
```

Flow:

```text
Subject
   |
   | pushes data
   v
Observer
```

Use when:

- event payload is small
- observers need the same data
- explicit event data is clearer

---

### Pull Model

The Subject sends itself or a reference:

```java
observer.update(this);
```

Observer pulls required state:

```java
public void update(Order order) {
    String status = order.getStatus();
}
```

Flow:

```text
Subject
   |
   | notification
   v
Observer
   |
   | getter calls
   v
Subject
```

Trade-off:

- flexible
- but Observer becomes more coupled to Subject API

---

## 12. Better Event Payload Design

Instead of passing many arguments:

```java
observer.update(orderId, status, customerId, timestamp);
```

use an event object:

```java
public class OrderEvent {

    private final String orderId;
    private final String status;

    public OrderEvent(String orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
}
```

Observer:

```java
public interface OrderObserver {
    void update(OrderEvent event);
}
```

This scales better than constantly changing method arguments.

---

## 13. Observer vs Strategy

### Strategy

```text
Context
   |
   | chooses/delegates to
   v
ONE Strategy
```

Purpose:

```text
interchangeable behavior / algorithm
```

Example:

```text
PaymentService
   |
   v
UPI Strategy
```

### Observer

```text
Subject
   |
   | notifies
   +--> Observer A
   +--> Observer B
   +--> Observer C
```

Purpose:

```text
one-to-many notification
```

Summary:

```text
Strategy
-> interchangeable behavior

Observer
-> notify many listeners when something changes
```

---

## 14. Observer vs Pub/Sub

Classic Observer:

```text
Subject
  |
  +--> Observer A
  +--> Observer B
```

The Subject usually directly knows Observer references.

Pub/Sub:

```text
Publisher
   |
   v
Message Broker / Event Bus
   |
   +--> Subscriber A
   +--> Subscriber B
```

Examples:

- Kafka
- RabbitMQ
- SNS
- event bus

Key distinction:

```text
Observer
-> commonly in-process and direct reference based

Pub/Sub
-> often broker mediated and possibly distributed
```

They are conceptually related, but not identical.

---

## 15. Synchronous Observer

The simple implementation is synchronous:

```java
for (OrderObserver observer : observers) {
    observer.update(status);
}
```

Flow:

```text
Subject thread
    |
    +--> Email
    |
    +--> SMS
    |
    +--> Analytics
```

The Subject waits for every Observer.

### Problem

If observers are slow:

```text
Email      = 2 sec
SMS        = 3 sec
Analytics  = 5 sec
```

then the operation can become slow.

---

## 16. Asynchronous Observer

A system may instead dispatch work through:

```text
Subject
   |
   v
Executor / Queue / Event Bus
   |
   +--> Observer A
   +--> Observer B
   +--> Observer C
```

But async handling introduces extra design concerns:

- retries
- ordering
- failure handling
- thread safety
- eventual consistency
- duplicate processing
- shutdown/lifecycle handling

Do not make Observer asynchronous automatically.

---

## 17. Observer Failure Handling

Suppose:

```java
for (OrderObserver observer : observers) {
    observer.update(status);
}
```

and the first Observer throws an exception.

Then later Observers may never execute.

```text
Email throws exception
        |
        X
SMS not called
Analytics not called
```

A possible isolation approach:

```java
for (OrderObserver observer : observers) {
    try {
        observer.update(status);
    } catch (Exception ex) {
        System.err.println(
                "Observer failed: " + ex.getMessage()
        );
    }
}
```

But the correct failure behavior depends on business semantics.

Questions to ask:

- Should one observer failure stop the entire operation?
- Should failed observers be retried?
- Is notification best-effort or guaranteed?
- Is the state change already committed?

---

## 18. Thread Safety

Basic learning implementation:

```java
private final List<OrderObserver> observers =
        new ArrayList<OrderObserver>();
```

This is not safe for arbitrary concurrent modification.

If one thread calls:

```java
addObserver(...)
```

while another iterates over the same list, concurrency issues can occur.

For read-heavy subscriber collections, one possible choice is:

```java
private final List<OrderObserver> observers =
        new CopyOnWriteArrayList<OrderObserver>();
```

Import:

```java
import java.util.concurrent.CopyOnWriteArrayList;
```

Use this only if concurrency requirements justify it.

---

## 19. Java 8 Lambda Usage

If Observer is a functional interface:

```java
@FunctionalInterface
public interface OrderObserver {
    void update(String status);
}
```

we can register a lambda:

```java
order.addObserver(
        status -> System.out.println(
                "Lambda observer: " + status
        )
);
```

This is still conceptually Observer Pattern.

The concrete observer behavior is represented using a lambda instead of a named class.

---

## 20. Open/Closed Principle

Observer supports extension by adding new listeners.

Before:

```text
Observer
  |
  +-- Email
  +-- SMS
```

After:

```text
Observer
  |
  +-- Email
  +-- SMS
  +-- Slack
  +-- Analytics
```

The Subject remains unchanged.

---

## 21. Common Real-World Use Cases

Observer is useful for:

- order status updates
- notification systems
- UI event listeners
- stock price listeners
- domain events
- cache invalidation listeners
- file watchers
- dashboard updates
- audit listeners
- workflow state listeners

---

## 22. When NOT to Use Observer

Do not introduce Observer just because an event exists.

If there is only:

```text
one caller
one direct dependency
one deterministic action
```

then direct invocation may be clearer:

```java
emailService.sendEmail();
```

Instead of introducing:

```text
Subject
Observer interface
Registration methods
Notification loop
Lifecycle handling
```

Use Observer when loose coupling and one-to-many reaction are genuinely useful.

---

## 23. Common Interview Traps

### Trap 1: "Observer means Kafka"

False.

Observer can be completely in memory.

Kafka is closer to distributed pub/sub or event streaming.

---

### Trap 2: "Observer is automatically asynchronous"

False.

Classic Observer is often synchronous.

Async behavior is an additional architectural choice.

---

### Trap 3: "Observers should know one another"

False.

Observers normally do not need references to other Observers.

The Subject coordinates notification.

---

### Trap 4: "One Subject has only one Observer"

Observer Pattern is fundamentally designed for one-to-many relationships.

---

### Trap 5: "Notify before or after state change does not matter"

It matters.

Observers must understand whether the event represents:

- intent
- state currently changing
- successfully completed state change

The event semantics must be explicit.

---

## 24. Complete Java 8 Copyable Example

```java
package Behavioral.Observer;

import java.util.ArrayList;
import java.util.List;

interface OrderObserver {
    void update(String status);
}

class EmailObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "Email notification: order status = " + status
        );
    }
}

class SmsObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "SMS notification: order status = " + status
        );
    }
}

class AnalyticsObserver implements OrderObserver {

    @Override
    public void update(String status) {
        System.out.println(
                "Analytics updated for status = " + status
        );
    }
}

class Order {

    private String status;

    private final List<OrderObserver> observers =
            new ArrayList<OrderObserver>();

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void setStatus(String status) {
        this.status = status;
        notifyObservers();
    }

    private void notifyObservers() {
        for (OrderObserver observer : observers) {
            observer.update(status);
        }
    }
}

public class Client {

    public static void main(String[] args) {

        Order order = new Order();

        OrderObserver emailObserver =
                new EmailObserver();

        OrderObserver smsObserver =
                new SmsObserver();

        OrderObserver analyticsObserver =
                new AnalyticsObserver();

        order.addObserver(emailObserver);
        order.addObserver(smsObserver);
        order.addObserver(analyticsObserver);

        order.setStatus("SHIPPED");

        order.removeObserver(smsObserver);

        System.out.println("--- After removing SMS observer ---");

        order.setStatus("DELIVERED");
    }
}
```

---

## 25. Fast Comparison Table

| Pattern | Main Question | Relationship |
|---|---|---|
| Strategy | Which behavior/algorithm should I use? | Context HAS-A Strategy |
| Observer | Who should react when something changes? | Subject HAS-A many Observers |
| Factory | Which implementation/object should I create? | Factory creates/selects Product |
| Builder | How should I construct this complex object? | Builder creates Product step by step |
| Prototype | Which existing object should I copy? | Prototype creates copy of itself |

---

## 26. Interview Mental Model

If the interviewer says:

> Whenever X changes, A, B, and C should react.

Think:

```text
             X
          Subject
             |
          notify
      /      |      \
     v       v       v
    A        B        C
```

Examples:

```text
Order status changes
-> Email
-> SMS
-> Analytics

Stock price changes
-> Dashboard
-> Alert service
-> Trading algorithm

UI button clicked
-> Event listeners
```

---

## 27. High-Yield Interview Questions

### Q1. What problem does Observer solve?

It decouples an event-producing Subject from multiple event-consuming Observers.

### Q2. What is the relationship between Subject and Observer?

```text
Subject HAS-A collection of Observer references.
```

### Q3. Is Observer synchronous or asynchronous?

The pattern itself does not require either. Classic in-process implementations are often synchronous. Async dispatch is an additional design choice.

### Q4. What is push vs pull Observer?

Push sends event/state data directly to Observer. Pull tells Observer something changed and lets it query Subject state.

### Q5. How is Observer different from Strategy?

Strategy provides one interchangeable behavior to a Context. Observer distributes one event/state change to multiple listeners.

### Q6. How is Observer different from pub/sub?

Classic Observer typically uses direct object references. Pub/sub often introduces a broker/event bus and may cross process or machine boundaries.

### Q7. What happens if one Observer fails?

In synchronous notification, an exception can prevent later Observers from running unless failures are isolated. The correct behavior depends on consistency requirements.

### Q8. How do you add a new reaction?

Implement the Observer interface and register the new Observer with the Subject. Subject code should not need modification.

### Q9. What concurrency problem can arise?

Concurrent registration/removal while iterating over a non-thread-safe Observer collection can cause race conditions or iteration failures.

### Q10. Why can Observer become difficult to debug?

A single state change can trigger many indirect reactions, possibly asynchronously. Event ownership, ordering, failure handling, and tracing must remain explicit.

---

## 28. Final Revision Summary

```text
Observer Pattern
================

Purpose:
Notify multiple interested objects when a Subject changes state or emits an event.

Structure:

Subject
  |
  | HAS-A many
  v
Observer interface
  ^
  |
Concrete Observers

Core operations:

addObserver()
removeObserver()
notifyObservers()
update()

Key idea:
Subject knows the Observer abstraction,
not the internal behavior of each listener.

Important interview topics:

- one-to-many relationship
- push vs pull
- sync vs async
- Observer vs pub/sub
- Observer vs Strategy
- failure isolation
- thread safety
- dynamic registration/removal
- Java 8 functional interface/lambda usage
```

### One sentence to remember

> **Observer encapsulates one-to-many notification: a Subject maintains interested Observers and notifies them through a common interface when relevant state or events change.**
