# Chain of Responsibility Pattern — Revision Notes

## 1. Core Idea

Use **Chain of Responsibility (CoR)** when a request should move through a sequence of handlers.

Each handler can:

```text
process the request
reject/stop the request
pass the request to the next handler
```

Mental model:

```text
Request
   |
   v
Handler A
   |
   v
Handler B
   |
   v
Handler C
```

---

## 2. What Problem Does It Solve?

Without CoR, one class may accumulate unrelated processing responsibilities:

```text
RequestProcessor
    |
    +-- Authentication
    +-- Authorization
    +-- Validation
    +-- Rate limiting
    +-- Logging
    +-- Audit
```

This causes tight coupling and poor separation of concerns.

CoR splits responsibilities:

```text
Request
   |
   v
AuthenticationHandler
   |
   v
AuthorizationHandler
   |
   v
ValidationHandler
   |
   v
RateLimitHandler
```

Each handler focuses on one job.

---

## 3. Core Architecture

```text
                 Handler
              <<abstract>>
          +------------------+
          | next: Handler    |
          +------------------+
          | setNext()        |
          | handle()         |
          +--------+---------+
                   ^
                   |
         +---------+---------+
         |         |         |
         v         v         v
      HandlerA  HandlerB  HandlerC
```

Relationships:

```text
ConcreteHandler IS-A Handler

Handler HAS-A next Handler
```

---

## 4. Base Handler

```java
public abstract class RequestHandler {

    protected RequestHandler nextHandler;

    public RequestHandler setNext(
            RequestHandler nextHandler
    ) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public abstract void handle(Request request);

    protected void handleNext(Request request) {

        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}
```

Important field:

```java
protected RequestHandler nextHandler;
```

This creates the chain.

---

## 5. Why Does setNext() Return nextHandler?

```java
public RequestHandler setNext(
        RequestHandler nextHandler
) {
    this.nextHandler = nextHandler;
    return nextHandler;
}
```

This allows:

```java
authentication
        .setNext(authorization)
        .setNext(validation);
```

First:

```text
authentication.next = authorization
```

and `authorization` is returned.

Then:

```text
authorization.next = validation
```

This differs from Builder chaining, where methods usually return `this`.

---

## 6. Request Object

```java
public class Request {

    private final String token;
    private final String role;
    private final String body;

    public Request(
            String token,
            String role,
            String body
    ) {
        this.token = token;
        this.role = role;
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getBody() {
        return body;
    }
}
```

---

## 7. Authentication Handler

```java
public class AuthenticationHandler
        extends RequestHandler {

    @Override
    public void handle(Request request) {

        if (request.getToken() == null) {
            throw new RuntimeException(
                    "Authentication failed"
            );
        }

        System.out.println(
                "Authentication successful"
        );

        handleNext(request);
    }
}
```

---

## 8. Authorization Handler

```java
public class AuthorizationHandler
        extends RequestHandler {

    @Override
    public void handle(Request request) {

        if (!"ADMIN".equals(request.getRole())) {
            throw new RuntimeException(
                    "Authorization failed"
            );
        }

        System.out.println(
                "Authorization successful"
        );

        handleNext(request);
    }
}
```

---

## 9. Validation Handler

```java
public class ValidationHandler
        extends RequestHandler {

    @Override
    public void handle(Request request) {

        if (request.getBody() == null
                || request.getBody().isEmpty()) {

            throw new RuntimeException(
                    "Validation failed"
            );
        }

        System.out.println(
                "Validation successful"
        );

        handleNext(request);
    }
}
```

---

## 10. Client Code

```java
public class Client {

    public static void main(String[] args) {

        RequestHandler authentication =
                new AuthenticationHandler();

        RequestHandler authorization =
                new AuthorizationHandler();

        RequestHandler validation =
                new ValidationHandler();

        authentication
                .setNext(authorization)
                .setNext(validation);

        Request request =
                new Request(
                        "valid-token",
                        "ADMIN",
                        "request-body"
                );

        authentication.handle(request);
    }
}
```

Flow:

```text
Authentication
      |
      v
Authorization
      |
      v
Validation
      |
      v
End
```

---

## 11. Early Termination

A handler can stop the chain.

Example:

```java
if (request.getToken() == null) {
    throw new RuntimeException(
            "Authentication failed"
    );
}
```

Then:

```text
Authentication
      |
      X
   failed

Authorization
      X

Validation
      X
```

This is a key CoR feature.

---

## 12. Two Common CoR Styles

### Pipeline style

Every handler may process and pass onward:

```text
Logging
   |
   v
Authentication
   |
   v
Authorization
   |
   v
Validation
```

### First-capable-handler style

Handlers pass until one can handle the request:

```text
Request
   |
   v
Handler A
   |
   | cannot handle
   v
Handler B
   |
   | CAN handle
   v
STOP
```

Both are valid CoR designs.

---

## 13. Support Escalation Example

```text
Basic Ticket
   |
   v
Level1Support
   |
   | cannot handle
   v
Level2Support
   |
   | cannot handle
   v
ManagerSupport
```

Example logic:

```java
public void handle(Ticket ticket) {

    if (ticket.getLevel() == BASIC) {

        resolve(ticket);
        return;
    }

    handleNext(ticket);
}
```

Only one handler may ultimately process the request.

---

## 14. Why Not Just Use a List?

Alternative:

```java
List<RequestHandler> handlers =
        new ArrayList<RequestHandler>();
```

Then:

```java
for (RequestHandler handler : handlers) {
    handler.handle(request);
}
```

This is valid.

Difference:

```text
Classic CoR
→ each handler controls forwarding

List-based pipeline
→ an external pipeline controls traversal
```

Frameworks often use an explicit chain/pipeline object instead of storing `nextHandler` directly.

---

## 15. Servlet Filter Analogy

Classic Java web filters are a strong analogy:

```java
public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
) {

    // before downstream

    chain.doFilter(request, response);

    // after downstream
}
```

Conceptually:

```text
Request
   |
   v
Filter A
   |
   v
Filter B
   |
   v
Filter C
   |
   v
Servlet / Controller
```

Handlers may execute logic both before and after downstream processing.

---

## 16. Spring Security Analogy

Conceptually:

```text
HTTP Request
     |
     v
Security Filter
     |
     v
Authentication Filter
     |
     v
Authorization-related processing
     |
     v
Application
```

The real framework is more sophisticated, but the chain intuition is highly relevant.

---

## 17. Order Matters

These are not equivalent:

```text
Authentication
      |
      v
Authorization
```

vs.

```text
Authorization
      |
      v
Authentication
```

Similarly:

```text
RateLimit
   |
   v
Expensive DB Lookup
```

may be preferable to doing the expensive work first.

Chain order is part of system behavior.

---

## 18. What If Nobody Handles the Request?

Possible outcomes:

```text
Do nothing
Return "not handled"
Throw exception
Use default handler
Fallback
Dead-letter
```

This should be explicit.

Example:

```text
Handler A
   |
   v
Handler B
   |
   v
Handler C
   |
   v
DefaultHandler
```

---

## 19. Dynamic Chain Composition

Chains can vary by environment/use case.

Production:

```text
Logging
   |
   v
Authentication
   |
   v
RateLimit
   |
   v
Authorization
   |
   v
Validation
```

Testing:

```text
Authentication
   |
   v
Validation
```

Internal endpoint:

```text
Logging
   |
   v
Validation
```

Handlers stay reusable.

---

## 20. CoR vs Observer

Observer:

```text
Subject
  / | \
 v  v  v
A  B  C
```

Fan-out notification.

CoR:

```text
Request
  |
  v
A
  |
  v
B
  |
  v
C
```

Sequential processing.

```text
Observer = fan-out
CoR      = forwarding/pipeline
```

---

## 21. CoR vs Command

Command:

```text
Invoker
   |
   v
Command
   |
   v
Receiver
```

Encapsulates an action/request.

CoR:

```text
Request
   |
   v
Handler
   |
   v
Handler
```

Processes/routes the request through handlers.

They can be combined.

---

## 22. CoR vs Decorator

Decorator:

```text
Decorator
   |
   v
Decorator
   |
   v
Component
```

Intent:

```text
add behavior around an object
```

CoR:

```text
Handler
   |
   v
Handler
   |
   v
Handler
```

Intent:

```text
process/forward a request through handlers
```

---

## 23. Advantages

```text
Loose coupling
Single-responsibility handlers
Flexible composition
Easy reordering
Easy add/remove handlers
Early termination
Reusable processing stages
```

---

## 24. Disadvantages

```text
Harder debugging in long chains
Order-dependent behavior
Request may remain unhandled
Many small classes
Hidden control flow
Performance overhead in long chains
```

---

## 25. When to Use

Good signals:

```text
Request passes through multiple stages

Multiple objects may handle a request

Sender should not know the exact receiver

Handlers need runtime composition

Processing may stop early
```

Common examples:

```text
HTTP middleware
Servlet filters
Security filters
Validation pipelines
Logging pipelines
Approval workflows
Support escalation
Exception handling
Event-processing pipelines
```

---

## 26. When Not to Use

If one stable component does the job:

```java
validator.validate(request);
```

then introducing a full handler chain may be unnecessary.

Use CoR when there is genuinely a chain of independently varying responsibilities.

---

## 27. Common Interview Traps

### Trap 1
"Every handler must process the request."

No. A handler may skip or stop.

### Trap 2
"CoR always finds one handler."

No. Pipeline-style chains often let many handlers participate.

### Trap 3
"Handler order does not matter."

Wrong. Ordering can change semantics.

### Trap 4
"CoR means linked list implementation."

No. The pattern's intent matters more than the exact storage structure.

A list-based pipeline can still reflect the same idea.

---

## 28. Interview Mental Model

When you see:

```text
Request
   ↓
Step 1
   ↓
Step 2
   ↓
Step 3
```

and each step may:

```text
process
pass onward
or stop
```

think:

```text
Chain of Responsibility
```

Core sentence:

> Chain of Responsibility passes a request through a sequence of handlers, allowing each handler to process, reject, or forward the request without tightly coupling the sender to a specific receiver.

---

## 29. Fast Revision Table

| Concept | Meaning |
|---|---|
| Handler | Common processing abstraction |
| Concrete Handler | Performs one responsibility |
| nextHandler | Reference to next stage |
| Early termination | Stop chain when appropriate |
| Pipeline style | Multiple handlers participate |
| First-capable style | Stop at first matching handler |
| Ordering | Affects semantics |
| Dynamic composition | Chain can be rearranged |

---

## 30. One-Line Memory Aid

```text
Strategy = HOW

Command = WHAT ACTION

Observer = WHO GETS NOTIFIED

State = HOW BEHAVIOR CHANGES WITH STATE

Chain of Responsibility = WHO IN THE CHAIN SHOULD PROCESS / PASS THE REQUEST
```
