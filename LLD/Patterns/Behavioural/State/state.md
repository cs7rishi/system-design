# State Pattern — Revision Notes

## 1. Core Idea

Use the **State Pattern** when an object's behavior changes depending on its current internal state.

Instead of:

```java
if (status == CREATED) {
    ...
} else if (status == PAID) {
    ...
} else if (status == SHIPPED) {
    ...
}
```

we move state-specific behavior into separate classes.

Mental model:

```text
                 Context
                   |
                   | currentState
                   v
                  State
                   ^
                   |
        +----------+----------+
        |          |          |
     Created      Paid      Shipped
      State       State      State
```

## 2. What Problem Does State Solve?

Without State Pattern, the same status checks tend to spread across multiple methods.

```java
public class Order {

    private String status;

    public void pay() {
        if ("CREATED".equals(status)) {
            status = "PAID";
        } else {
            System.out.println("Payment not allowed");
        }
    }

    public void ship() {
        if ("PAID".equals(status)) {
            status = "SHIPPED";
        } else {
            System.out.println("Shipping not allowed");
        }
    }
}
```

Problems:

```text
Large switch/if blocks
Repeated state checks
Transition rules spread across the Context
Harder to extend with new states
```

## 3. Core Architecture

```text
                 Order
                Context
                  |
                  | HAS-A
                  v
              OrderState
             <<interface>>
                  ^
                  |
       +----------+----------+
       |          |          |
    Created      Paid      Shipped
     State       State      State
```

Relationships:

```text
Order HAS-A OrderState

CreatedState IS-A OrderState
PaidState IS-A OrderState
ShippedState IS-A OrderState
```

## 4. State Interface

```java
public interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void cancel(Order order);
}
```

## 5. Context

```java
public class Order {

    private OrderState state;

    public Order() {
        this.state = new CreatedState();
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public void pay() {
        state.pay(this);
    }

    public void ship() {
        state.ship(this);
    }

    public void cancel() {
        state.cancel(this);
    }
}
```

## 6. Concrete States

```java
public class CreatedState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("Payment successful");
        order.setState(new PaidState());
    }

    @Override
    public void ship(Order order) {
        System.out.println("Cannot ship before payment");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order cancelled");
        order.setState(new CancelledState());
    }
}
```

```java
public class PaidState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("Order is already paid");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Order shipped");
        order.setState(new ShippedState());
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order cancelled");
        order.setState(new CancelledState());
    }
}
```

```java
public class ShippedState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("Order already paid and shipped");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Order already shipped");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Cannot cancel a shipped order");
    }
}
```

```java
public class CancelledState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("Cannot pay a cancelled order");
    }

    @Override
    public void ship(Order order) {
        System.out.println("Cannot ship a cancelled order");
    }

    @Override
    public void cancel(Order order) {
        System.out.println("Order is already cancelled");
    }
}
```

## 7. Client Code

```java
public class Client {

    public static void main(String[] args) {

        Order order = new Order();

        order.pay();
        order.ship();
        order.cancel();
    }
}
```

## 8. State Transition Diagram

```text
              pay
CREATED  ------------>  PAID
   |                       |
   | cancel                | ship
   v                       v
CANCELLED               SHIPPED
                            |
                            | deliver
                            v
                        DELIVERED
```

## 9. Who Changes the State?

Common approach:

```java
order.setState(new PaidState());
```

The current concrete state performs the transition.

Alternative designs can make the Context own transitions, but that can reintroduce large conditionals.

## 10. State vs Strategy

```text
Strategy
→ HOW should an operation be performed?
→ Client usually selects the strategy.

State
→ HOW should the object behave in its current lifecycle state?
→ State transitions usually drive the next behavior.
```

Key distinction:

```text
Strategy = behavior is selected
State    = behavior evolves as state changes
```

## 11. Why Not Just Use an Enum?

Enums are fine for simple status values:

```java
public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    CANCELLED
}
```

Use State when each state owns substantial behavior, rules, or transitions.

## 12. Enum With Behavior

```java
public enum OrderState {

    CREATED {
        @Override
        public void handle() {
            System.out.println("Created logic");
        }
    },

    PAID {
        @Override
        public void handle() {
            System.out.println("Paid logic");
        }
    };

    public abstract void handle();
}
```

Useful for small fixed state machines.

## 13. State Object Reuse

If states are stateless, they can be reused:

```java
public final class PaidState implements OrderState {

    public static final PaidState INSTANCE = new PaidState();

    private PaidState() {
    }
}
```

Then:

```java
order.setState(PaidState.INSTANCE);
```

## 14. State + Factory

Useful when restoring persisted status:

```java
public class OrderStateFactory {

    public static OrderState getState(OrderStatus status) {

        switch (status) {

            case CREATED:
                return new CreatedState();

            case PAID:
                return new PaidState();

            case SHIPPED:
                return new ShippedState();

            case CANCELLED:
                return new CancelledState();

            default:
                throw new IllegalArgumentException("Unsupported state");
        }
    }
}
```

## 15. State Pattern and State Machines

State Pattern is an OO design pattern.

A state machine is a model of:

```text
States
Transitions
Allowed events
```

State Pattern is one way to implement state-machine-like behavior.

## 16. Common Use Cases

```text
Order lifecycle
Payment lifecycle
Vending machine
ATM
Elevator
Traffic light
Media player
Document workflow
Support ticket
Network connection lifecycle
Game character state
```

## 17. Advantages

```text
Removes repeated state conditionals
Encapsulates state-specific behavior
Makes transitions explicit
Keeps Context smaller
Improves extensibility
```

## 18. Disadvantages

```text
More classes
Potential state-class explosion
Transition logic can become scattered
Overkill for simple status checks
```

## 19. Common Interview Traps

```text
State and Strategy are NOT the same.
Not every enum needs State Pattern.
Not every transition should be allowed.
State Pattern is not identical to a state machine.
```

## 20. Interview Mental Model

If many methods repeatedly do:

```java
switch (status) {
    ...
}
```

ask:

```text
Does behavior significantly depend on lifecycle state?
```

If yes, State Pattern may fit.

Core sentence:

> State Pattern encapsulates state-specific behavior into separate state objects and lets the Context delegate behavior to its current state.

## 21. Fast Revision Table

| Concept | Meaning |
|---|---|
| Context | Object whose behavior changes |
| State | Common behavior contract |
| Concrete State | State-specific implementation |
| Transition | Move from one state to another |
| `state.method(this)` | Context delegates to current state |
| State vs Strategy | Evolving lifecycle vs algorithm selection |
| Enum vs State | Value-only status vs encapsulated behavior |

## 22. One-Line Memory Aid

```text
Strategy = HOW

Command = WHAT ACTION

Observer = WHO GETS NOTIFIED

State = HOW BEHAVIOR CHANGES WITH CURRENT STATE
```
