# Facade Pattern — Revision Notes

## 1. Core Idea

Use the **Facade Pattern** when a subsystem is complex and clients need a simpler, high-level interface.

> Facade provides a simplified, high-level interface to a complex subsystem while leaving the subsystem available for advanced use when appropriate.

Mental model:

```text
                    Client
                       |
                       v
                    Facade
                       |
          +------------+------------+
          |            |            |
          v            v            v
      Subsystem A  Subsystem B  Subsystem C
```

The Facade coordinates subsystem classes so callers do not need to know all implementation details.

---

## 2. What Problem Does Facade Solve?

Suppose placing an order requires:

```text
Check inventory
      ↓
Reserve inventory
      ↓
Process payment
      ↓
Create shipment
      ↓
Send notification
```

Without Facade, every caller may need to know:

```text
which subsystem classes exist
which methods to call
what order to call them in
how the workflow is coordinated
```

This causes duplicated orchestration and tight coupling.

---

## 3. Example Subsystem

### InventoryService

```java
public class InventoryService {

    public boolean isAvailable(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Checking inventory"
        );

        return true;
    }

    public void reserve(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Inventory reserved"
        );
    }
}
```

### PaymentService

```java
public class PaymentService {

    public void processPayment(
            String customerId,
            double amount
    ) {
        System.out.println(
                "Payment processed: " + amount
        );
    }
}
```

### ShippingService

```java
public class ShippingService {

    public void createShipment(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Shipment created"
        );
    }
}
```

### NotificationService

```java
public class NotificationService {

    public void sendConfirmation(
            String customerId
    ) {
        System.out.println(
                "Order confirmation sent"
        );
    }
}
```

---

## 4. Without Facade

Client has to coordinate everything:

```java
InventoryService inventory =
        new InventoryService();

PaymentService payment =
        new PaymentService();

ShippingService shipping =
        new ShippingService();

NotificationService notification =
        new NotificationService();

if (!inventory.isAvailable(productId, quantity)) {
    throw new RuntimeException(
            "Product unavailable"
    );
}

inventory.reserve(productId, quantity);

payment.processPayment(
        customerId,
        amount
);

shipping.createShipment(
        productId,
        quantity
);

notification.sendConfirmation(
        customerId
);
```

Dependency picture:

```text
Client
  |
  +--> InventoryService
  +--> PaymentService
  +--> ShippingService
  +--> NotificationService
```

---

## 5. Introduce the Facade

```java
public class OrderFacade {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    public OrderFacade(
            InventoryService inventoryService,
            PaymentService paymentService,
            ShippingService shippingService,
            NotificationService notificationService
    ) {
        this.inventoryService =
                inventoryService;

        this.paymentService =
                paymentService;

        this.shippingService =
                shippingService;

        this.notificationService =
                notificationService;
    }

    public void placeOrder(
            String customerId,
            String productId,
            int quantity,
            double amount
    ) {

        if (!inventoryService.isAvailable(
                productId,
                quantity
        )) {

            throw new RuntimeException(
                    "Product unavailable"
            );
        }

        inventoryService.reserve(
                productId,
                quantity
        );

        paymentService.processPayment(
                customerId,
                amount
        );

        shippingService.createShipment(
                productId,
                quantity
        );

        notificationService.sendConfirmation(
                customerId
        );
    }
}
```

The complicated workflow is now hidden behind:

```java
placeOrder(...)
```

---

## 6. Client Code

```java
public class Client {

    public static void main(String[] args) {

        OrderFacade facade =
                new OrderFacade(
                        new InventoryService(),
                        new PaymentService(),
                        new ShippingService(),
                        new NotificationService()
                );

        facade.placeOrder(
                "C100",
                "P100",
                2,
                1000
        );
    }
}
```

The Client thinks in a high-level business operation:

```text
place order
```

instead of low-level subsystem coordination.

---

## 7. Architecture Before and After

Before:

```text
                 Client
              /    |    \
             /     |     \
            v      v      v
       Inventory Payment Shipping
             \
              v
         Notification
```

After:

```text
                 Client
                    |
                    v
               OrderFacade
              /    |    |    \
             v     v    v     v
        Inventory Pay Shipping Notify
```

The Facade reduces the number of dependencies visible to the Client.

---

## 8. What Does Facade Hide?

Facade can hide:

```text
number of subsystem classes
call ordering
workflow orchestration
configuration details
provider selection
data conversion
complex setup
```

The goal is to expose meaningful high-level operations.

---

## 9. Facade Can Have Multiple Methods

Facade does not mean "one giant method."

Example:

```java
public class OrderFacade {

    public void placeOrder(...) {
    }

    public void cancelOrder(...) {
    }

    public void returnOrder(...) {
    }

    public OrderStatus getOrderStatus(...) {
        ...
    }
}
```

These should represent coherent high-level operations.

---

## 10. Facade Does Not Necessarily Block Direct Access

Facade simplifies access.

It does not automatically make subsystem classes inaccessible.

```text
Facade = simplified path
not necessarily exclusive path
```

A caller may still use a subsystem directly when advanced control is appropriate.

---

## 11. Relationships

Facade usually:

```text
HAS-A / USES subsystem components
```

Example:

```text
OrderFacade HAS-A InventoryService
OrderFacade HAS-A PaymentService
OrderFacade HAS-A ShippingService
OrderFacade HAS-A NotificationService
```

Unlike Decorator, Facade does not normally need to implement the same interface as the subsystem.

---

## 12. Facade vs Adapter

```text
Adapter
→ compatibility problem
→ translate one interface into another

Facade
→ complexity problem
→ expose a simpler high-level interface
```

Memory aid:

```text
Adapter = COMPATIBILITY

Facade = SIMPLICITY
```

---

## 13. Facade vs Decorator

```text
Decorator
→ adds behavior
→ preserves same abstraction

Facade
→ simplifies access
→ exposes higher-level operations
```

Memory aid:

```text
Decorator = ENHANCEMENT

Facade = SIMPLIFICATION
```

---

## 14. Facade vs Proxy

```text
Proxy
→ controls access to a real subject

Facade
→ simplifies access to a subsystem
```

Memory aid:

```text
Proxy = CONTROL ACCESS

Facade = SIMPLIFY ACCESS
```

---

## 15. Facade vs Mediator

Facade:

```text
Client
   |
   v
Facade
   |
   v
Subsystem
```

Primarily simplifies external access.

Mediator:

```text
Component A
     \
      \
    Mediator
      /
     /
Component B
```

Coordinates communication among peer components.

Summary:

```text
Facade = external simplified entry point

Mediator = internal peer coordination
```

---

## 16. Real-World Analogy

Hotel reception:

```text
Guest
  |
  v
Reception
  |
  +--> Housekeeping
  +--> Restaurant
  +--> Maintenance
  +--> Taxi
```

The receptionist acts as a Facade over many departments.

---

## 17. Video Conversion Example

Complex subsystem:

```text
CodecFactory
VideoDecoder
AudioDecoder
BitrateConverter
AudioMixer
FileWriter
```

Facade:

```java
VideoConverter converter =
        new VideoConverter();

converter.convert(
        "video.mov",
        "mp4"
);
```

Architecture:

```text
Client
   |
   v
VideoConverterFacade
   |
   +--> Codec
   +--> Decoder
   +--> Encoder
   +--> Mixer
   +--> Writer
```

---

## 18. Facade in Service Layers

Service-layer orchestration often behaves like a Facade:

```java
public class CheckoutService {

    private InventoryService inventory;
    private PaymentService payment;
    private ShippingService shipping;

    public Order checkout(
            CheckoutRequest request
    ) {

        inventory.reserve(...);

        payment.charge(...);

        shipping.schedule(...);

        return ...;
    }
}
```

From the caller's perspective:

```java
checkoutService.checkout(request);
```

is a high-level entry point over multiple lower-level operations.

---

## 19. Facade and Dependency Injection

Facade does not require manual construction.

With Spring:

```java
@Service
public class OrderFacade {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;

    public OrderFacade(
            InventoryService inventoryService,
            PaymentService paymentService,
            ShippingService shippingService
    ) {
        this.inventoryService =
                inventoryService;

        this.paymentService =
                paymentService;

        this.shippingService =
                shippingService;
    }
}
```

Dependency Injection and Facade solve different problems.

---

## 20. Should Facade Contain Business Logic?

A Facade should mainly:

```text
coordinate
orchestrate
simplify
translate high-level requests
```

Avoid turning it into:

```text
God Object
```

Business rules should generally stay in the appropriate domain/service components.

Mental model:

```text
Facade
→ orchestration

Subsystem
→ specialized behavior
```

---

## 21. Bad Facade

Bad:

```java
public class ApplicationFacade {

    public void createUser() {}
    public void deleteUser() {}
    public void processPayment() {}
    public void shipOrder() {}
    public void generateReport() {}
    public void calculateTax() {}
    // hundreds more
}
```

A good Facade should represent a coherent boundary.

Better:

```text
OrderFacade
PaymentFacade
ReportingFacade
```

instead of:

```text
EverythingFacade
```

---

## 22. Multiple Facades Are Fine

Same subsystem may have different simplified views:

```text
Subsystem
   ^
   |
   +---- CustomerFacade
   |
   +---- AdminFacade
   |
   +---- ReportingFacade
```

Different clients can receive different high-level APIs.

---

## 23. Facade Can Depend on Interfaces

Better real-world design:

```java
private final PaymentProcessor paymentProcessor;
```

instead of tightly coupling to a concrete provider.

Then:

```text
OrderFacade
    |
    v
PaymentProcessor
    ^
    |
Adapter / Strategy / Implementation
```

Patterns combine naturally.

---

## 24. Facade + Adapter

```text
Client
   |
   v
OrderFacade
   |
   v
PaymentProcessor
   |
   v
PaymentAdapter
   |
   v
ThirdPartyGateway
```

Responsibilities:

```text
Facade
→ simplify overall workflow

Adapter
→ translate incompatible provider API
```

---

## 25. Facade + Strategy

Facade:

```text
orderFacade.placeOrder(...)
```

Internally:

```text
PaymentStrategy
   |
   +-- UPI
   +-- Card
   +-- PayPal
```

Summary:

```text
Facade = simple entry point

Strategy = interchangeable algorithm
```

---

## 26. Facade + Factory

Facade may ask a Factory to select/create an implementation.

```text
Facade = simplify/orchestrate

Factory = create/select

Adapter = translate

Strategy = interchangeable behavior
```

Understanding these boundaries is more important than memorizing isolated diagrams.

---

## 27. Advantages

```text
Simpler client API
Reduced coupling
Centralized orchestration
Fewer visible dependencies
Cleaner entry points
Subsystem can evolve behind the Facade
```

Before:

```text
Client -> A
Client -> B
Client -> C
Client -> D
```

After:

```text
Client -> Facade -> A/B/C/D
```

---

## 28. Disadvantages

```text
Facade can become a God Object
Adds another abstraction layer
May hide important subsystem choices
Clients may depend too heavily on one giant Facade
```

The goal is not to hide everything.

The goal is to provide a useful simplified boundary.

---

## 29. When to Use

Good signals:

```text
Client coordinates too many classes

Several callers repeat the same multi-step workflow

Complex library needs a simple entry point

Callers should not depend on subsystem details
```

Typical examples:

```text
Checkout workflows
SDK wrappers
Video processing
Home automation
Cloud APIs
Legacy subsystems
Service orchestration
Compiler subsystems
```

---

## 30. When Not to Use

Avoid when:

```text
Subsystem is already simple

Client legitimately needs fine-grained control

Facade just forwards every method 1:1

Facade would become an unrelated God Object
```

---

## 31. Full Java 8 Example

### InventoryService

```java
public class InventoryService {

    public boolean isAvailable(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Checking inventory"
        );

        return true;
    }

    public void reserve(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Inventory reserved"
        );
    }
}
```

### PaymentService

```java
public class PaymentService {

    public void processPayment(
            String customerId,
            double amount
    ) {
        System.out.println(
                "Payment processed: " + amount
        );
    }
}
```

### ShippingService

```java
public class ShippingService {

    public void createShipment(
            String productId,
            int quantity
    ) {
        System.out.println(
                "Shipment created"
        );
    }
}
```

### NotificationService

```java
public class NotificationService {

    public void sendConfirmation(
            String customerId
    ) {
        System.out.println(
                "Order confirmation sent"
        );
    }
}
```

### Facade

```java
public class OrderFacade {

    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    public OrderFacade(
            InventoryService inventoryService,
            PaymentService paymentService,
            ShippingService shippingService,
            NotificationService notificationService
    ) {
        this.inventoryService =
                inventoryService;

        this.paymentService =
                paymentService;

        this.shippingService =
                shippingService;

        this.notificationService =
                notificationService;
    }

    public void placeOrder(
            String customerId,
            String productId,
            int quantity,
            double amount
    ) {

        if (!inventoryService.isAvailable(
                productId,
                quantity
        )) {

            throw new RuntimeException(
                    "Product unavailable"
            );
        }

        inventoryService.reserve(
                productId,
                quantity
        );

        paymentService.processPayment(
                customerId,
                amount
        );

        shippingService.createShipment(
                productId,
                quantity
        );

        notificationService.sendConfirmation(
                customerId
        );
    }
}
```

### Client

```java
public class Client {

    public static void main(String[] args) {

        OrderFacade facade =
                new OrderFacade(
                        new InventoryService(),
                        new PaymentService(),
                        new ShippingService(),
                        new NotificationService()
                );

        facade.placeOrder(
                "C100",
                "P100",
                2,
                1000
        );
    }
}
```

---

## 32. Architecture to Remember

```text
                       Client
                          |
                          | simple API
                          v
                     OrderFacade
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
     Inventory         Payment         Shipping
                                           |
                          +----------------+
                          |
                          v
                    Notification
```

Key relationship:

```text
Facade HAS-A / USES subsystem components
```

---

## 33. Common Interview Traps

### Trap 1
"Facade replaces the subsystem."

No. It provides a simpler entry point to it.

### Trap 2
"Facade prevents direct subsystem access."

Not necessarily.

### Trap 3
"Facade should contain all business logic."

No. Prefer orchestration and simplification.

### Trap 4
"Facade and Adapter are the same."

No.

```text
Adapter = compatibility
Facade = simplification
```

---

## 34. Interview Mental Model

If you hear:

> "Using this subsystem requires calling many classes in the correct sequence."

Think:

```text
Facade
```

If you hear:

> "Controllers/callers should not know all these lower-level services."

Again:

```text
Facade
```

Core sentence:

> Facade provides a simplified, high-level interface to a complex subsystem.

---

## 35. Fast Revision Table

| Concept | Meaning |
|---|---|
| Client | Uses simplified API |
| Facade | High-level entry point |
| Subsystem | Lower-level specialized classes |
| Main goal | Simplification |
| Access restriction | Not inherent |
| Business logic | Prefer subsystem/domain; Facade orchestrates |
| Multiple Facades | Valid for different client views |

---

## 36. One-Line Structural Memory Aid

```text
Adapter = compatibility

Decorator = behavior enhancement

Facade = simplification

Proxy = access control
```
