# Adapter Pattern — Revision Notes

## Core Idea
**Adapter** converts an existing incompatible interface into the interface expected by the consuming code.

```text
Client -> Target <- Adapter -> Adaptee
```

## Terminology
- **Client:** code consuming the Target abstraction.
- **Target:** interface the Client expects.
- **Adapter:** translator implementing Target.
- **Adaptee:** existing incompatible class.

## What Exactly Is Client?
`Client` is a **design role**, not necessarily an HTTP client, browser, external service, or end user. It is simply the code using the abstraction.

```java
PaymentProcessor processor = ...;
processor.pay(1000);
```

The code containing these calls is acting as the Client.

## Java 8 Example

### Target
```java
public interface PaymentProcessor {
    void pay(double amount);
}
```

### Adaptee
```java
public class LegacyPaymentGateway {
    public void makePayment(String customerId, double amount) {
        System.out.println("Legacy payment: " + amount + " for " + customerId);
    }
}
```

### Object Adapter
```java
public class LegacyPaymentAdapter implements PaymentProcessor {
    private final LegacyPaymentGateway gateway;
    private final String customerId;

    public LegacyPaymentAdapter(LegacyPaymentGateway gateway, String customerId) {
        this.gateway = gateway;
        this.customerId = customerId;
    }

    @Override
    public void pay(double amount) {
        gateway.makePayment(customerId, amount);
    }
}
```

### Client
```java
public class Client {
    public static void main(String[] args) {
        LegacyPaymentGateway gateway = new LegacyPaymentGateway();

        PaymentProcessor processor =
                new LegacyPaymentAdapter(gateway, "CUSTOMER-123");

        processor.pay(1000);
    }
}
```

## Relationships
```text
LegacyPaymentAdapter IS-A PaymentProcessor
LegacyPaymentAdapter HAS-A LegacyPaymentGateway
```

Object Adapter uses composition and is generally the cleaner Java default.

## What Can an Adapter Translate?
```text
method names
parameters
units
request/response models
return types
exceptions
vendor status codes
```

Example: application amount -> provider amount in cents.

## Architectural Boundary
```text
Application
    |
    v
Application-owned Target interface
---------------- integration boundary
    |
    v
Adapter
    |
    v
Third-party / legacy API
```

This keeps vendor-specific request types, responses, exceptions, and APIs from leaking throughout application code.

## Object Adapter vs Class Adapter
**Object Adapter:** Adapter HAS-A Adaptee (composition).

**Class Adapter:** Adapter extends Adaptee and implements Target. Java permits one class superclass plus interfaces, but not multiple class inheritance. Prefer composition unless inheritance is genuinely appropriate.

## Adapter + Factory
Factory can select an Adapter like any other implementation.

```text
Factory = WHICH implementation?
Adapter = HOW do incompatible interfaces communicate?
```

## Adapter + Strategy
An Adapter can implement an application Strategy interface while internally translating to a vendor SDK.

## Comparisons
```text
Adapter   = compatibility / translate interface
Decorator = add behavior while preserving abstraction
Facade    = simplify a complex subsystem
Proxy     = control access to another object
```

## Java Example
`InputStreamReader` is a useful Adapter-style example:

```text
InputStream (bytes)
      |
      v
InputStreamReader
      |
      v
Reader (characters)
```

## Advantages
- Reuse legacy/existing implementations.
- Integrate third-party SDKs.
- Isolate vendor-specific APIs.
- Translate requests, responses, units, and exceptions.
- Improve replaceability.

## Risks
- Extra indirection/classes.
- Translation can become complex.
- Do not hide fundamentally different business semantics behind a misleading common interface.

## Interview Mental Model
If you hear: **"The existing/third-party API does what we need, but its interface does not match ours"**, think **Adapter**.

> Adapter converts the interface of an existing class into the interface expected by the client.

## Fast Revision
| Role | Meaning |
|---|---|
| Client | Consuming/calling code |
| Target | Expected interface |
| Adapter | Translator |
| Adaptee | Existing incompatible implementation |
| Object Adapter | Composition |
| Main goal | Interface compatibility |
