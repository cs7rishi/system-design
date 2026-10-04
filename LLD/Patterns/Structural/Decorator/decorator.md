# Decorator Pattern — Revision Notes

## 1. Core Idea

Use the **Decorator Pattern** when you want to add behavior/responsibilities to an object dynamically without modifying the original class.

> Decorator dynamically adds responsibilities to an object by wrapping it in another object that implements the same interface.

Mental model:

```text
        Component
           ^
           |
   +-------+--------+
   |                |
ConcreteComponent  Decorator
                      |
                      | wraps
                      v
                  Component
```

The critical structural signature is:

```text
Decorator IS-A Component

Decorator HAS-A Component
```

---

## 2. What Problem Does Decorator Solve?

Suppose a coffee can have optional add-ons:

```text
Milk
Sugar
Caramel
Chocolate
Whipped Cream
```

Inheritance quickly creates combinations:

```text
Coffee
CoffeeWithMilk
CoffeeWithSugar
CoffeeWithMilkAndSugar
CoffeeWithMilkAndCaramel
CoffeeWithMilkSugarAndCaramel
...
```

This leads to combinatorial subclass explosion.

Decorator replaces subclass combinations with runtime composition.

---

## 3. Component Interface

```java
public interface Coffee {

    String getDescription();

    double getCost();
}
```

This is the common abstraction implemented by both base components and decorators.

---

## 4. Concrete Component

```java
public class SimpleCoffee
        implements Coffee {

    @Override
    public String getDescription() {
        return "Simple Coffee";
    }

    @Override
    public double getCost() {
        return 50.0;
    }
}
```

`SimpleCoffee` is the base object being decorated.

---

## 5. Base Decorator

```java
public abstract class CoffeeDecorator
        implements Coffee {

    protected final Coffee coffee;

    protected CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}
```

Key relationships:

```text
CoffeeDecorator IS-A Coffee

CoffeeDecorator HAS-A Coffee
```

That combination is the core of the pattern.

---

## 6. Milk Decorator

```java
public class MilkDecorator
        extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {

        return coffee.getDescription()
                + ", Milk";
    }

    @Override
    public double getCost() {

        return coffee.getCost()
                + 20.0;
    }
}
```

---

## 7. Sugar Decorator

```java
public class SugarDecorator
        extends CoffeeDecorator {

    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }

    @Override
    public String getDescription() {

        return coffee.getDescription()
                + ", Sugar";
    }

    @Override
    public double getCost() {

        return coffee.getCost()
                + 10.0;
    }
}
```

---

## 8. Client Code

```java
public class Client {

    public static void main(String[] args) {

        Coffee coffee =
                new SimpleCoffee();

        coffee =
                new MilkDecorator(coffee);

        coffee =
                new SugarDecorator(coffee);

        System.out.println(
                coffee.getDescription()
        );

        System.out.println(
                coffee.getCost()
        );
    }
}
```

Output:

```text
Simple Coffee, Milk, Sugar
80.0
```

---

## 9. Object Graph

After wrapping:

```text
coffee
   |
   v
SugarDecorator
   |
   v
MilkDecorator
   |
   v
SimpleCoffee
```

This is a wrapper chain.

---

## 10. Call Flow

Calling:

```java
coffee.getCost();
```

executes:

```text
SugarDecorator.getCost()
        |
        v
MilkDecorator.getCost()
        |
        v
SimpleCoffee.getCost()
        |
        v
       50
```

Then values return outward:

```text
SimpleCoffee = 50

MilkDecorator = 50 + 20 = 70

SugarDecorator = 70 + 10 = 80
```

---

## 11. Why the Same Interface Matters

Every layer is a `Coffee`:

```text
SimpleCoffee IS-A Coffee
MilkDecorator IS-A Coffee
SugarDecorator IS-A Coffee
```

Therefore decorators can wrap:

```text
ConcreteComponent
or
another Decorator
```

That enables stacking.

---

## 12. Why Do We Need SimpleCoffee?

A Decorator is conceptually supposed to decorate an existing valid Component.

Good:

```text
SugarDecorator
      |
      v
MilkDecorator
      |
      v
SimpleCoffee
```

If decorators allow `null`:

```text
MilkDecorator
      |
      v
     null
```

then the decorator is doing two jobs:

```text
sometimes acts as decorator
sometimes acts as base component
```

This weakens the design.

`SimpleCoffee` supplies the actual/base behavior:

```text
SimpleCoffee = base price/description

MilkDecorator = adds milk behavior

SugarDecorator = adds sugar behavior
```

---

## 13. Why Not Use null as the Base?

Technically possible:

```java
if (coffee != null) {
    return coffee.getCost() + 20;
}

return 20;
```

But this has downsides:

```text
Every decorator needs null checks
No strong invariant that a decorator wraps a Component
Decorator semantics become less clear
More branches and repeated code
```

Preferred invariant:

```text
Every Decorator ALWAYS wraps a valid Component
```

---

## 14. Enforcing Non-Null Wrapped Component

Java 8:

```java
import java.util.Objects;

protected CoffeeDecorator(Coffee coffee) {

    this.coffee =
            Objects.requireNonNull(
                    coffee,
                    "Coffee cannot be null"
            );
}
```

Then decorators can safely call:

```java
coffee.getCost();
```

without repeated null checks.

---

## 15. Null Object Alternative

If "empty component" has valid domain meaning, use an object rather than `null`.

```java
public class EmptyCoffee
        implements Coffee {

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public double getCost() {
        return 0;
    }
}
```

Then:

```java
Coffee coffee =
        new MilkDecorator(
                new EmptyCoffee()
        );
```

This reflects the **Null Object Pattern** idea.

However, for a coffee domain, `SimpleCoffee` usually has better semantics.

---

## 16. Multiple Concrete Components

Decorators do not require specifically `SimpleCoffee`.

You could have:

```java
public class Espresso implements Coffee {
    ...
}
```

```java
public class Latte implements Coffee {
    ...
}
```

Then:

```java
Coffee coffee =
        new MilkDecorator(
                new Espresso()
        );
```

The rule is:

```text
Decorator requires SOME valid Component
```

not:

```text
Decorator requires SimpleCoffee specifically
```

---

## 17. Runtime Composition

```java
Coffee coffee =
        new SimpleCoffee();

if (wantsMilk) {
    coffee =
            new MilkDecorator(coffee);
}

if (wantsSugar) {
    coffee =
            new SugarDecorator(coffee);
}
```

This allows behavior to be assembled dynamically at runtime.

---

## 18. Open/Closed Principle

Add a new decorator without modifying existing classes:

```java
public class ChocolateDecorator
        extends CoffeeDecorator {

    public ChocolateDecorator(
            Coffee coffee
    ) {
        super(coffee);
    }

    @Override
    public String getDescription() {
        return coffee.getDescription()
                + ", Chocolate";
    }

    @Override
    public double getCost() {
        return coffee.getCost()
                + 25.0;
    }
}
```

Existing components/decorators remain unchanged.

---

## 19. Why Not Boolean Flags?

Alternative:

```java
private boolean milk;
private boolean sugar;
private boolean caramel;
```

Then:

```java
if (milk) ...
if (sugar) ...
if (caramel) ...
```

This makes one class know every possible optional feature.

Decorator moves each optional responsibility into its own class.

---

## 20. Decorator vs Adapter

```text
Adapter
→ changes/translates interface
→ solves compatibility

Decorator
→ preserves interface
→ adds behavior
```

Memory aid:

```text
Adapter = CHANGE INTERFACE

Decorator = KEEP INTERFACE, ADD BEHAVIOR
```

---

## 21. Decorator vs Proxy

Both often wrap another object and implement the same interface.

Difference is intent:

```text
Decorator
→ enrich/add behavior

Proxy
→ control access
```

Proxy examples:

```text
authorization
lazy initialization
remote access
caching
access control
```

---

## 22. Decorator vs Strategy

Strategy:

```text
Context HAS-A Strategy
```

and delegates one behavior.

Decorator:

```text
Decorator IS-A Component
Decorator HAS-A Component
```

Decorators may recursively stack.

---

## 23. Decorator vs Inheritance

Inheritance:

```text
BaseClass
   ^
   |
Subclass
```

Behavior is tied to class hierarchy.

Decorator:

```text
Decorator
   |
   v
Component
```

Behavior is assembled dynamically.

Summary:

```text
Inheritance
→ static/class-based extension

Decorator
→ runtime/object-based extension
```

---

## 24. Java I/O Example

Decorator-style structure appears in Java I/O:

```java
InputStream in =
        new FileInputStream("file.txt");
```

Wrap it:

```java
InputStream buffered =
        new BufferedInputStream(in);
```

Or:

```java
InputStream data =
        new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("file.txt")
                )
        );
```

Conceptually:

```text
DataInputStream
      |
      v
BufferedInputStream
      |
      v
FileInputStream
```

Each layer adds capability while preserving the stream abstraction.

---

## 25. Realistic Notification Example

```java
public interface NotificationSender {
    void send(String message);
}
```

Base sender:

```java
public class EmailSender
        implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println(
                "Email: " + message
        );
    }
}
```

Logging decorator:

```java
public class LoggingDecorator
        implements NotificationSender {

    private final NotificationSender delegate;

    public LoggingDecorator(
            NotificationSender delegate
    ) {
        this.delegate = delegate;
    }

    @Override
    public void send(String message) {

        System.out.println(
                "Logging notification"
        );

        delegate.send(message);
    }
}
```

Composition:

```java
NotificationSender sender =
        new LoggingDecorator(
                new EmailSender()
        );
```

---

## 26. Before/After Behavior

A decorator can surround the delegated call:

```java
@Override
public void send(String message) {

    validate(message);

    delegate.send(message);

    recordMetrics();
}
```

So decoration is not limited to appending values.

---

## 27. Is an Abstract Decorator Mandatory?

No.

You may directly implement:

```java
public class MilkDecorator
        implements Coffee {
}
```

The abstract base decorator is a convenience when all decorators share:

```java
protected final Coffee coffee;
```

It reduces duplication.

---

## 28. Order Matters

These may behave differently:

```java
new LoggingDecorator(
        new CompressionDecorator(component)
);
```

versus:

```java
new CompressionDecorator(
        new LoggingDecorator(component)
);
```

Decorator ordering is part of runtime behavior.

---

## 29. Object Identity

```java
Coffee original =
        new SimpleCoffee();

Coffee decorated =
        new MilkDecorator(original);
```

Then:

```java
original == decorated
```

is `false`.

Decorator wraps the original object; it does not mutate its runtime class.

---

## 30. Advantages

```text
Add behavior dynamically
Avoid subclass explosion
Preserve same abstraction
Compose features at runtime
Supports Open/Closed Principle
Each decorator has one focused responsibility
```

---

## 31. Disadvantages

```text
Many small wrapper objects
Order-dependent behavior
Long chains can be hard to debug
Object identity can become less obvious
Configuration can become verbose
```

---

## 32. When to Use

Good signals:

```text
Optional behaviors/features
Many feature combinations
Need runtime composition
Cannot/should not modify original class
Inheritance combinations are exploding
```

Examples:

```text
Java I/O streams
Logging wrappers
Metrics wrappers
Compression
Encryption
Caching
UI components
Notification pipelines
```

---

## 33. When Not to Use

Avoid when:

```text
There is only one simple fixed behavior
Subclassing is genuinely simpler and stable
Wrapper chains would add more complexity than value
```

---

## 34. Interview Architecture Diagram

```text
                 <<interface>>
                   Component
                      ^
                      |
          +-----------+-----------+
          |                       |
 ConcreteComponent             Decorator
                                  |
                                  | HAS-A
                                  v
                               Component
                                  ^
                                  |
                        +---------+---------+
                        |                   |
                  DecoratorA          DecoratorB
```

Essential relationships:

```text
Decorator IS-A Component

Decorator HAS-A Component
```

---

## 35. Common Interview Traps

### Trap 1
"Decorator is just inheritance."

No. The key mechanism is runtime wrapping/composition.

### Trap 2
"Decorator and Adapter are the same because both wrap objects."

No. Adapter changes interface; Decorator preserves interface and adds behavior.

### Trap 3
"Decorator can wrap null."

Mechanically possible, but usually weakens the invariant. Prefer a valid Component or a meaningful Null Object.

### Trap 4
"Abstract Decorator is mandatory."

No. It is a convenience.

---

## 36. Interview Mental Model

If the interviewer says:

> "We need optional behavior that can be combined dynamically without modifying the original class."

Think:

```text
Decorator
```

If they say:

> "Subclass combinations are exploding."

Again:

```text
Decorator
```

Core sentence:

> Decorator dynamically adds responsibilities to an object by wrapping it in another object that implements the same interface.

---

## 37. Fast Revision Table

| Concept | Meaning |
|---|---|
| Component | Common abstraction |
| Concrete Component | Base object |
| Decorator | Wrapper implementing same abstraction |
| Wrapped component | Object being decorated |
| Key relation | Decorator IS-A and HAS-A Component |
| Main benefit | Dynamic behavior composition |
| Null base | Usually avoid; require valid component |
| Order | Decorator order can matter |

---

## 38. One-Line Memory Aid

```text
Adapter = change/translate interface

Decorator = preserve interface, add behavior
```
