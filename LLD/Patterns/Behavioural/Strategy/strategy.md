# Strategy Pattern — Revision Notes

> **Primary language:** Java  
> **Category:** Behavioral Design Pattern  
> **Focus:** Interchangeable behavior, composition, runtime algorithm selection, and Strategy + Factory

---

## 1. Intent

The **Strategy Pattern** encapsulates multiple interchangeable algorithms or behaviors behind a common interface and allows a context to delegate the varying behavior to the selected strategy.

Use it when:

- several algorithms perform the same conceptual operation,
- behavior needs to vary at runtime,
- large `if/else` or `switch` blocks contain substantial behavior,
- algorithms should be independently testable,
- or a class is taking responsibility for too many variants of one behavior.

### Core mental model

```text
Factory
-------
Which object / implementation should I create?

Builder
-------
How should this complex object be constructed?

Prototype
---------
Which existing object should I copy?

Strategy
--------
Which algorithm / behavior should I use?
```

---

## 2. The Problem Strategy Solves

Suppose payment behavior is implemented directly inside one service:

```java
public class PaymentService {

    public void pay(String paymentType, double amount) {
        if (paymentType.equals("CREDIT_CARD")) {
            // card validation
            // fee calculation
            // gateway call
            // response handling
        } else if (paymentType.equals("UPI")) {
            // UPI validation
            // provider call
            // response handling
        } else if (paymentType.equals("PAYPAL")) {
            // PayPal authentication
            // provider call
            // response handling
        }
    }
}
```

As payment types grow, `PaymentService` becomes responsible for every payment algorithm:

```text
PaymentService
     |
     +-- Credit Card logic
     +-- UPI logic
     +-- PayPal logic
     +-- Crypto logic
     +-- Net Banking logic
```

Problems:

- large conditionals,
- unrelated algorithms mixed together,
- difficult unit testing,
- high coupling,
- frequent modification of the same class,
- violations of Single Responsibility,
- poor extensibility.

---

## 3. Identify the Varying Behavior

Ask:

> What changes independently while the high-level operation stays the same?

For payments, the stable operation is:

```text
"Pay an amount"
```

The varying algorithms are:

```text
Credit Card
UPI
PayPal
```

So extract the behavior into a common contract:

```java
public interface PaymentStrategy {
    void pay(double amount);
}
```

---

## 4. Strategy Pattern Architecture

```text
                       Client
                         |
                         | chooses/provides
                         v
                  Concrete Strategy
                         |
                         | injected into
                         v
+------------------------------------------------+
|                 PaymentService                 |
|                    Context                     |
|                                                |
|  paymentStrategy : PaymentStrategy             |
+-------------------------+----------------------+
                          |
                          | delegates
                          v
                  <<interface>>
                 PaymentStrategy
                 +-------------+
                 | pay(amount) |
                 +------+------+ 
                        ^
                        |
           +------------+------------+
           |            |            |
+----------------+ +----------+ +----------------+
| CreditCard     | |   UPI    | | PayPal         |
| Strategy       | | Strategy | | Strategy       |
+----------------+ +----------+ +----------------+
```

### Important relationships

```text
ConcreteStrategy IS-A Strategy
PaymentService   HAS-A Strategy
```

This distinction is important in LLD interviews.

---

## 5. Strategy Roles

### Strategy
Defines the common algorithm contract.

```java
public interface PaymentStrategy {
    void pay(double amount);
}
```

### Concrete Strategy
Implements one particular algorithm.

```java
public class UpiPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using UPI");
    }
}
```

### Context
Uses a strategy instead of implementing all variants itself.

```java
public class PaymentService {

    private final PaymentStrategy paymentStrategy;

    public PaymentService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(double amount) {
        paymentStrategy.pay(amount);
    }
}
```

### Client
Selects or obtains the desired strategy and gives it to the context.

---

## 6. Basic Java Implementation

### `PaymentStrategy.java`

```java
package Behavioral.Strategy;

public interface PaymentStrategy {
    void pay(double amount);
}
```

### `CreditCardPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Credit Card");
    }
}
```

### `UpiPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class UpiPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using UPI");
    }
}
```

### `PaypalPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class PaypalPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using PayPal");
    }
}
```

### `PaymentService.java`

```java
package Behavioral.Strategy;

public class PaymentService {

    private final PaymentStrategy paymentStrategy;

    public PaymentService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(double amount) {
        paymentStrategy.pay(amount);
    }
}
```

### Client

```java
package Behavioral.Strategy;

public class Client {

    public static void main(String[] args) {
        PaymentStrategy strategy = new UpiPaymentStrategy();
        PaymentService paymentService = new PaymentService(strategy);

        paymentService.checkout(1000.0);
    }
}
```

---

## 7. What Happens at Runtime?

```java
PaymentStrategy strategy = new UpiPaymentStrategy();
```

creates a concrete strategy object:

```text
strategy
   |
   v
+----------------------+ 
| UpiPaymentStrategy   |
+----------------------+
```

Then:

```java
PaymentService service = new PaymentService(strategy);
```

creates the context:

```text
service
   |
   v
+------------------------+
| PaymentService         |
| strategy --------------+------+
+------------------------+      |
                                v
                      +----------------------+
                      | UpiPaymentStrategy   |
                      +----------------------+
```

Finally:

```java
service.checkout(1000.0);
```

delegates:

```text
PaymentService.checkout()
          |
          v
paymentStrategy.pay()
          |
          v
UpiPaymentStrategy.pay()
```

The context does not know the implementation details.

---

## 8. Constructor Injection vs Setter Injection

### Constructor injection

Use when a valid context must always have a strategy and the strategy normally remains stable.

```java
public PaymentService(PaymentStrategy strategy) {
    this.paymentStrategy = strategy;
}
```

Benefits:

- context cannot exist without its dependency,
- field can be `final`,
- easier reasoning,
- naturally supports immutability of the dependency reference.

### Setter injection

Use when changing strategy during the context's lifetime is a real domain requirement.

```java
public void setPaymentStrategy(PaymentStrategy strategy) {
    this.paymentStrategy = strategy;
}
```

Example:

```java
service.setPaymentStrategy(new UpiPaymentStrategy());
service.checkout(1000);

service.setPaymentStrategy(new CreditCardPaymentStrategy());
service.checkout(2000);
```

Do not prefer setter injection merely because Strategy *can* change at runtime. Choose it only when runtime mutation is meaningful.

---

## 9. Strategy Is Composition Over Inheritance

Strategy usually uses composition:

```java
class PaymentService {
    private PaymentStrategy paymentStrategy;
}
```

Relationship:

```text
PaymentService HAS-A PaymentStrategy
```

This is more flexible than creating subclasses such as:

```text
PaymentService
    ^
    |
+---+------------------+
|                      |
UPIPaymentService   CardPaymentService
```

A context can also compose multiple independent strategies:

```text
CheckoutService
     |
     +-- PaymentStrategy
     +-- DiscountStrategy
     +-- ShippingStrategy
```

This avoids subclass explosions such as:

```text
UpiPremiumCustomerExpressShippingCheckoutService
```

---

## 10. Strategy and Open/Closed Principle

Adding a new strategy should not require modifying the context.

Example:

```java
public class CryptoPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Crypto");
    }
}
```

`PaymentService` remains unchanged.

```text
Before:
PaymentStrategy
  +-- UPI
  +-- CreditCard
  +-- PayPal

After:
PaymentStrategy
  +-- UPI
  +-- CreditCard
  +-- PayPal
  +-- Crypto
```

The system is extended by adding a new implementation.

---

## 11. Strategy Does NOT Eliminate Every Conditional

A common interview mistake is saying:

> Strategy removes all `if/else` or `switch` statements.

Not necessarily.

If an API receives:

```text
paymentType = UPI
```

something must map that value to:

```text
UpiPaymentStrategy
```

Strategy removes the **algorithm implementation** from the conditional.

Bad:

```text
if UPI
    50 lines of UPI processing
else if CARD
    80 lines of card processing
else if PAYPAL
    70 lines of PayPal processing
```

Better:

```text
UPI    -> UpiPaymentStrategy
CARD   -> CreditCardPaymentStrategy
PAYPAL -> PaypalPaymentStrategy
```

Selection is a separate responsibility.

---

# 12. Strategy + Factory — Recommended Selection Design

Strategy and Factory solve different problems and work naturally together.

```text
Factory
------
Which strategy should I select/create?

Strategy
--------
How should the selected behavior execute?
```

Architecture:

```text
                Client
                  |
                  | PaymentType.UPI
                  v
        PaymentStrategyFactory
                  |
                  | selects/creates
                  v
           PaymentStrategy
                  |
                  v
           PaymentService
                  |
                  | delegates
                  v
        Concrete Strategy
```

---

## 13. Full Strategy + Factory Implementation

### `PaymentType.java`

```java
package Behavioral.Strategy;

public enum PaymentType {
    CREDIT_CARD,
    UPI,
    PAYPAL
}
```

### `PaymentStrategy.java`

```java
package Behavioral.Strategy;

public interface PaymentStrategy {
    void pay(double amount);
}
```

### `CreditCardPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Credit Card");
    }
}
```

### `UpiPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class UpiPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using UPI");
    }
}
```

### `PaypalPaymentStrategy.java`

```java
package Behavioral.Strategy;

public class PaypalPaymentStrategy implements PaymentStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using PayPal");
    }
}
```

### `PaymentStrategyFactory.java`

```java
package Behavioral.Strategy;

public final class PaymentStrategyFactory {

    private PaymentStrategyFactory() {
    }

    public static PaymentStrategy getStrategy(PaymentType paymentType) {
        return switch (paymentType) {
            case CREDIT_CARD -> new CreditCardPaymentStrategy();
            case UPI -> new UpiPaymentStrategy();
            case PAYPAL -> new PaypalPaymentStrategy();
        };
    }
}
```

### `PaymentService.java`

```java
package Behavioral.Strategy;

public class PaymentService {

    private final PaymentStrategy paymentStrategy;

    public PaymentService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(double amount) {
        paymentStrategy.pay(amount);
    }
}
```

### `Client.java`

```java
package Behavioral.Strategy;

public class Client {

    public static void main(String[] args) {
        PaymentType paymentType = PaymentType.UPI;

        PaymentStrategy strategy =
                PaymentStrategyFactory.getStrategy(paymentType);

        PaymentService paymentService =
                new PaymentService(strategy);

        paymentService.checkout(1000.0);
    }
}
```

Expected output:

```text
Paid 1000.0 using UPI
```

---

## 14. Responsibility Breakdown in Strategy + Factory

```text
Client
------
Knows what payment type was requested.

PaymentStrategyFactory
----------------------
Knows which strategy corresponds to a PaymentType.

PaymentStrategy
---------------
Defines the common payment contract.

ConcreteStrategy
----------------
Knows how one payment algorithm works.

PaymentService
--------------
Knows when payment should happen and delegates execution.
```

This is better than putting both selection and payment algorithms inside `PaymentService`.

---

## 15. Why Is a `switch` Inside the Factory Acceptable?

A Factory containing:

```java
switch (paymentType) {
    ...
}
```

is not automatically bad design.

The key improvement is that the conditional performs only **selection**:

```text
PaymentType -> Strategy
```

It does not contain the actual business algorithm.

Compare:

```text
Factory switch
-------------
UPI -> create UpiPaymentStrategy
```

with:

```text
Service switch
--------------
UPI -> validation + fee + network call + retries + response parsing + ...
```

The first is a small centralized creation decision. The second mixes major behaviors.

If strategy creation grows more dynamic, a registry or dependency-injection container can replace the switch.

---

## 16. Registry-Based Selection Alternative

For many strategies, selection can be registration-based:

```java
public class PaymentStrategyFactory {

    private final Map<PaymentType, PaymentStrategy> strategies;

    public PaymentStrategyFactory(Map<PaymentType, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy getStrategy(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment type: " + type
            );
        }

        return strategy;
    }
}
```

Conceptually:

```text
Registry
+-------------------------------+
| CREDIT_CARD -> CardStrategy   |
| UPI         -> UpiStrategy    |
| PAYPAL      -> PaypalStrategy |
+-------------------------------+
```

This is useful when strategies are injected or registered dynamically.

For basic interviews, the enum + Factory switch implementation is usually easier to explain first.

---

## 17. Why Use an Enum Instead of Raw Strings?

Prefer:

```java
PaymentType.UPI
```

over:

```java
"UPI"
```

Advantages:

- compile-time safety,
- no spelling mismatch,
- IDE autocomplete,
- exhaustive Java switch support,
- clearer domain modeling.

Bad:

```java
factory.getStrategy("UPii");
```

Better:

```java
factory.getStrategy(PaymentType.UPI);
```

---

## 18. Strategy and Dependency Injection

In a Spring application, concrete strategies can be beans:

```java
@Component
public class UpiPaymentStrategy implements PaymentStrategy {
    ...
}
```

A registry can be assembled from injected strategies rather than manually calling `new`.

The design-pattern idea remains the same:

```text
Context depends on abstraction
        |
        v
PaymentStrategy
        ^
        |
Concrete implementations
```

Spring/DI is an implementation mechanism, not a replacement for understanding Strategy.

---

## 19. Java Lambdas as Strategies

If the strategy interface has one abstract method:

```java
@FunctionalInterface
public interface DiscountStrategy {
    double discount(double amount);
}
```

then Java lambdas can represent strategies:

```java
DiscountStrategy premium = amount -> amount * 0.20;
DiscountStrategy regular = amount -> amount * 0.05;
```

This is still the same conceptual pattern:

```text
Context
  |
  v
Strategy behavior supplied externally
```

For complex business behavior, named classes are often easier to test, inject, observe, and maintain.

---

## 20. Real-World Strategy Examples

### Routing

```text
RouteStrategy
  +-- CarRouteStrategy
  +-- WalkingRouteStrategy
  +-- BikeRouteStrategy
  +-- PublicTransportRouteStrategy
```

Stable goal:

```text
Calculate route from A to B
```

Variable behavior:

```text
Driving / walking / cycling / transit algorithm
```

### Discounts

```text
DiscountStrategy
  +-- RegularDiscount
  +-- PremiumDiscount
  +-- FestivalDiscount
```

### Compression

```text
CompressionStrategy
  +-- ZipCompression
  +-- GzipCompression
```

### Sorting

Java's `Comparator<T>` is a strong conceptual example of supplying interchangeable comparison behavior.

---

## 21. Strategy vs Factory

| Question | Factory | Strategy |
|---|---|---|
| Primary concern | Object selection/creation | Behavior/algorithm selection |
| Main question | Which object should I create? | Which behavior should execute? |
| Common output | Concrete implementation | Algorithm implementation |
| Can work together? | Yes | Yes |

Typical combination:

```text
Factory selects Strategy
        |
        v
Context executes Strategy
```

---

## 22. Strategy vs State — Common Interview Confusion

Both may look like:

```text
Context HAS-A interface
       |
       +-- Implementation A
       +-- Implementation B
```

But intent differs.

### Strategy

Client/configuration usually chooses an algorithm:

```text
"Use this behavior."
```

Examples:

```text
UPI vs Card
QuickSort vs another algorithm
Walking vs driving route
```

### State

Behavior changes because the object's internal state changes:

```text
"The object is now in another state."
```

Examples:

```text
Order: CREATED -> PAID -> SHIPPED
Document: DRAFT -> MODERATION -> PUBLISHED
```

Remember:

```text
Strategy = interchangeable algorithm/policy
State    = behavior driven by lifecycle state
```

---

## 23. Strategy vs Template Method

### Strategy

Uses composition:

```text
Context HAS-A Strategy
```

Algorithms can be replaced at runtime.

### Template Method

Uses inheritance:

```text
BaseClass
   ^
   |
Subclass overrides steps
```

Use Strategy when algorithm replacement through composition is desired.
Use Template Method when subclasses share an invariant algorithm skeleton and customize certain steps.

---

## 24. Advantages

- isolates varying algorithms,
- replaces large behavioral conditionals,
- supports runtime substitution,
- promotes composition over inheritance,
- makes algorithms independently testable,
- reduces coupling between context and implementation,
- supports Open/Closed Principle,
- makes intent explicit through named strategies.

---

## 25. Disadvantages / Trade-Offs

- increases number of classes/objects,
- clients or factories must know how to choose a strategy,
- unnecessary for very small conditions,
- strategy interfaces can become awkward if algorithms need very different inputs,
- excessive abstraction can make simple business logic harder to follow.

Do not introduce Strategy merely to replace every two-branch `if`.

---

## 26. Common Mistakes

### Mistake 1: Concrete strategy checks its own type

Bad:

```java
public void pay(PaymentType type, double amount) {
    if (type == PaymentType.UPI) {
        ...
    }
}
```

A concrete strategy should already represent that specific behavior.

### Mistake 2: Context knows concrete classes

Avoid:

```java
if (...) {
    new UpiPaymentStrategy().pay(amount);
}
```

inside the Context.

The Context should depend on:

```java
PaymentStrategy
```

### Mistake 3: Put selection logic and algorithm logic together

Keep:

```text
selection -> Factory/Registry
behavior  -> Strategy
usage     -> Context
```

### Mistake 4: Creating Strategy when behavior is trivial and stable

A small `if` is sometimes the clearer design.

---

## 27. Interview Recognition Heuristic

When you see:

```java
if (type == A) {
    // substantial algorithm A
} else if (type == B) {
    // substantial algorithm B
} else if (type == C) {
    // substantial algorithm C
}
```

ask:

> Are these interchangeable implementations of the same conceptual behavior?

If yes, Strategy is a strong candidate.

Mental model:

```text
WHAT stays constant?
--------------------
Process payment

HOW varies?
-----------
UPI / Card / PayPal
```

---

## 28. High-Yield Interview Questions

### Q1. What problem does Strategy solve?
It encapsulates interchangeable algorithms behind a common interface so the context delegates varying behavior rather than implementing every variant itself.

### Q2. What relationship does Context have with Strategy?
A **HAS-A** relationship through composition.

### Q3. What relationship does ConcreteStrategy have with Strategy?
An **IS-A** relationship by implementing/extending the Strategy abstraction.

### Q4. Does Strategy eliminate all `if/else`?
No. Strategy removes substantial algorithm logic from conditionals. A small selection decision may remain in a Factory, registry, configuration layer, or DI container.

### Q5. Who chooses the strategy?
Usually the client, a Factory, a registry, configuration, or dependency-injection container.

### Q6. Can Strategy change at runtime?
Yes, if the Context exposes a meaningful mechanism for replacing it. Runtime mutability is optional, not mandatory.

### Q7. Why prefer composition here?
Algorithms can vary independently without creating context subclasses for every combination of behaviors.

### Q8. Strategy vs Factory?
Factory chooses/creates an implementation; Strategy defines interchangeable behavior. A Factory can select a Strategy.

### Q9. Strategy vs State?
Strategy represents a chosen algorithm or policy. State represents behavior that changes according to an object's lifecycle state.

### Q10. Why might `Comparator<T>` remind you of Strategy?
Sorting accepts comparison behavior externally, allowing different ordering algorithms/policies without changing the collection or sorting infrastructure.

---

## 29. Fast Revision Sheet

```text
PATTERN
-------
Strategy

CATEGORY
--------
Behavioral

INTENT
------
Encapsulate interchangeable algorithms behind a common contract.

PARTICIPANTS
------------
Client
Context
Strategy
ConcreteStrategy

RELATIONSHIPS
-------------
Context HAS-A Strategy
ConcreteStrategy IS-A Strategy

CORE FLOW
---------
Client selects Strategy
        |
        v
Context receives Strategy
        |
        v
Context delegates behavior
        |
        v
ConcreteStrategy executes

FACTORY COMBINATION
-------------------
PaymentType
    |
    v
StrategyFactory
    |
    v
PaymentStrategy
    |
    v
PaymentService

BEST CLUE
---------
Multiple substantial algorithms implement the same conceptual operation.

COMMON TRAP
-----------
"Strategy removes every conditional."
No: selection may remain, but algorithm bodies move out.
```

---

## 30. One-Sentence Interview Definition

> **Strategy Pattern encapsulates interchangeable algorithms behind a common interface and lets a Context delegate varying behavior through composition, with the concrete strategy selected by the client, configuration, Factory, or registry.**

---

## 31. Final Architecture to Remember

```text
                    Client
                      |
                PaymentType
                      |
                      v
            PaymentStrategyFactory
                      |
                      | selects
                      v
                <<interface>>
               PaymentStrategy
              /       |        \
             /        |         \
            v         v          v
         Card        UPI       PayPal
        Strategy   Strategy    Strategy
             \        |        /
              \       |       /
               \      |      /
                      v
                PaymentService
                    Context
                      |
                      | delegates
                      v
               selected Strategy
```

Remember the responsibility split:

```text
Factory  -> WHICH strategy?
Strategy -> HOW does behavior execute?
Context  -> WHEN/WHERE is behavior used?
```
