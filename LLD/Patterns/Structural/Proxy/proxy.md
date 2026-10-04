# Proxy Pattern — Revision Notes

## 1. Core Idea

Use the **Proxy Pattern** when you want a stand-in object that controls access to another real object.

> Proxy provides a substitute for another object and controls access to that object while usually preserving the same client-facing interface.

Mental model:

```text
Client
   |
   v
Subject
   ^
   |
 Proxy
   |
   v
RealSubject
```

Usually both `Proxy` and `RealSubject` implement the same interface.

---

## 2. What Problem Does Proxy Solve?

Suppose creating/loading a real object is expensive.

Example:

```java
public interface Image {
    void display();
}
```

Real implementation:

```java
public class RealImage implements Image {

    private final String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk();
    }

    private void loadFromDisk() {
        System.out.println(
                "Loading image from disk: " + fileName
        );
    }

    @Override
    public void display() {
        System.out.println(
                "Displaying image: " + fileName
        );
    }
}
```

Problem:

```java
Image image =
        new RealImage("photo.jpg");
```

loads immediately, even if `display()` is never called.

Proxy can delay or mediate that access.

---

## 3. Virtual Proxy

```java
public class ImageProxy implements Image {

    private final String fileName;

    private RealImage realImage;

    public ImageProxy(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void display() {

        if (realImage == null) {

            realImage =
                    new RealImage(fileName);
        }

        realImage.display();
    }
}
```

Flow:

```text
Client
  |
  v
ImageProxy
  |
  | lazy creation
  v
RealImage
```

Purpose:

```text
delay expensive creation/loading until needed
```

---

## 4. Why Same Interface?

Both:

```text
ImageProxy
RealImage
```

implement:

```text
Image
```

So the client can depend only on:

```java
Image image = ...;
```

and does not need to know whether it has a real object or a proxy.

---

## 5. Core Architecture

```text
              <<interface>>
                 Subject
                    ^
                    |
          +---------+---------+
          |                   |
        Proxy             RealSubject
          |
          | HAS-A
          v
      RealSubject
```

Essential relationships:

```text
Proxy IS-A Subject

RealSubject IS-A Subject

Proxy HAS-A RealSubject
```

---

## 6. Proxy vs Decorator

They often look structurally similar.

### Decorator

```text
Goal:
add behavior
```

### Proxy

```text
Goal:
control access
```

Memory aid:

```text
Decorator = ADD/ENRICH BEHAVIOR

Proxy = CONTROL/MEDIATE ACCESS
```

Intent matters more than code shape.

---

## 7. Protection Proxy

Use when access should depend on authorization.

```java
public interface DocumentService {

    void deleteDocument(
            String documentId
    );
}
```

Real service:

```java
public class RealDocumentService
        implements DocumentService {

    @Override
    public void deleteDocument(
            String documentId
    ) {
        System.out.println(
                "Deleted document: "
                + documentId
        );
    }
}
```

Proxy:

```java
public class DocumentServiceProxy
        implements DocumentService {

    private final DocumentService realService;
    private final String role;

    public DocumentServiceProxy(
            DocumentService realService,
            String role
    ) {
        this.realService = realService;
        this.role = role;
    }

    @Override
    public void deleteDocument(
            String documentId
    ) {

        if (!"ADMIN".equals(role)) {

            throw new SecurityException(
                    "Access denied"
            );
        }

        realService.deleteDocument(
                documentId
        );
    }
}
```

---

## 8. Caching Proxy

```java
public class CachedUserService
        implements UserService {

    private final UserService realService;

    private final Map<String, User> cache =
            new HashMap<String, User>();

    public CachedUserService(
            UserService realService
    ) {
        this.realService = realService;
    }

    @Override
    public User getUser(String id) {

        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        User user =
                realService.getUser(id);

        cache.put(id, user);

        return user;
    }
}
```

Flow:

```text
Client
  |
  v
Caching Proxy
  |
  +--> cache hit -> return
  |
  +--> cache miss -> RealSubject
```

---

## 9. Remote Proxy

A Remote Proxy represents an object that actually exists elsewhere.

Conceptually:

```text
Client
  |
  v
Local Proxy
  |
  v
Network
  |
  v
Remote Service
```

The proxy may hide:

```text
serialization
HTTP/RPC transport
network errors
response deserialization
```

The client still interacts with a local-looking abstraction.

---

## 10. Logging / Monitoring Proxy

```java
public class LoggingUserService
        implements UserService {

    private final UserService realService;

    public LoggingUserService(
            UserService realService
    ) {
        this.realService = realService;
    }

    @Override
    public User getUser(String id) {

        System.out.println(
                "Fetching user: " + id
        );

        User user =
                realService.getUser(id);

        System.out.println(
                "Fetched user: " + id
        );

        return user;
    }
}
```

This mediates access and observes calls.

---

## 11. Proxy vs Facade

```text
Facade
→ simplify access to many subsystem classes

Proxy
→ control access to one logical subject
```

Memory aid:

```text
Facade = SIMPLIFY ACCESS

Proxy = CONTROL ACCESS
```

---

## 12. Proxy vs Adapter

```text
Adapter
→ translate/change interface

Proxy
→ preserve interface and mediate access
```

Memory aid:

```text
Adapter = COMPATIBILITY

Proxy = ACCESS CONTROL
```

---

## 13. Proxy vs Decorator vs Adapter

| Pattern | Same interface as wrapped object? | Main intent |
|---|---:|---|
| Adapter | Often no | Translate interface |
| Decorator | Yes | Add behavior |
| Proxy | Yes | Control access |

Shortcut:

```text
Adapter   = compatibility
Decorator = enhancement
Proxy     = access control
```

---

## 14. Java Dynamic Proxy

Java provides:

```java
java.lang.reflect.Proxy
```

Example contract:

```java
public interface UserService {
    void createUser();
}
```

Real implementation:

```java
public class UserServiceImpl
        implements UserService {

    @Override
    public void createUser() {
        System.out.println(
                "Creating user"
        );
    }
}
```

Invocation handler:

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LoggingInvocationHandler
        implements InvocationHandler {

    private final Object target;

    public LoggingInvocationHandler(
            Object target
    ) {
        this.target = target;
    }

    @Override
    public Object invoke(
            Object proxy,
            Method method,
            Object[] args
    ) throws Throwable {

        System.out.println(
                "Before: "
                + method.getName()
        );

        Object result =
                method.invoke(
                        target,
                        args
                );

        System.out.println(
                "After: "
                + method.getName()
        );

        return result;
    }
}
```

Create proxy:

```java
UserService real =
        new UserServiceImpl();

UserService proxy =
        (UserService)
        Proxy.newProxyInstance(
                UserService.class
                        .getClassLoader(),
                new Class<?>[]{
                        UserService.class
                },
                new LoggingInvocationHandler(
                        real
                )
        );

proxy.createUser();
```

---

## 15. Why Dynamic Proxy Is Useful

Without runtime-generated proxies, you may write many repetitive wrappers:

```text
UserServiceProxy
OrderServiceProxy
PaymentServiceProxy
NotificationServiceProxy
```

Dynamic proxies can apply generic cross-cutting behavior:

```text
logging
metrics
authorization
auditing
transactions
```

---

## 16. JDK Dynamic Proxy Limitation

Classic JDK dynamic proxies work primarily with **interfaces**.

For concrete classes, frameworks may use subclass-based proxying / bytecode generation.

This is relevant to Spring proxy behavior.

---

## 17. Spring AOP and Proxy Intuition

Common proxy-backed concerns include:

```text
@Transactional
@Async
@Cacheable
security
AOP advice
```

Conceptually:

```text
Caller
  |
  v
Spring Proxy
  |
  | cross-cutting logic
  v
Target Bean
```

For transactions:

```text
start transaction
      ↓
call real method
      ↓
commit / rollback
```

---

## 18. Self-Invocation Caveat

Conceptually:

```java
public class OrderService {

    public void outer() {
        inner();
    }

    @Transactional
    public void inner() {
    }
}
```

Flow:

```text
External caller
    |
    v
Proxy
    |
    v
Target.outer()
    |
    | direct self-call
    v
Target.inner()
```

That internal call may bypass the external proxy path.

This is a practical reason to understand Proxy in Java frameworks.

---

## 19. ORM Lazy Loading Intuition

Proxy-like lazy behavior may represent data not yet loaded:

```text
Entity reference
   |
   v
Proxy
   |
   | first access
   v
Database load
   |
   v
Real data
```

This resembles Virtual Proxy.

---

## 20. Proxy Can Create or Receive RealSubject

### Lazy creation

```java
if (realImage == null) {
    realImage =
            new RealImage(fileName);
}
```

Useful for Virtual Proxy.

### Injected target

```java
public LoggingProxy(
        Service realService
) {
    this.realService = realService;
}
```

Useful for:

```text
logging
security
caching
metrics
```

---

## 21. Before / After Delegation

A proxy can execute policy before and after the real call:

```java
@Override
public Result execute(Request request) {

    authorize(request);

    long start =
            System.currentTimeMillis();

    Result result =
            realSubject.execute(request);

    recordMetrics(
            System.currentTimeMillis()
                    - start
    );

    return result;
}
```

Flow:

```text
before
  ↓
RealSubject
  ↓
after
```

---

## 22. Proxy Chains

Possible:

```text
Client
  |
  v
AuthorizationProxy
  |
  v
CachingProxy
  |
  v
LoggingProxy
  |
  v
RealService
```

This can be useful, but long chains may become hard to debug.

---

## 23. Main Proxy Types

```text
Virtual Proxy
→ lazy loading/creation

Protection Proxy
→ authorization/access control

Remote Proxy
→ represent remote object/service

Caching Proxy
→ cache expensive results

Logging/Monitoring Proxy
→ observe calls
```

---

## 24. Advantages

```text
Lazy initialization
Access control
Caching
Remote access abstraction
Logging/metrics
Cross-cutting policy isolation
Transparent substitution
```

---

## 25. Disadvantages

```text
Extra indirection
Harder debugging
Hidden behavior
Proxy chains can get complex
Performance overhead
Framework self-invocation surprises
```

---

## 26. When to Use

Good signals:

```text
Need to control whether/how real object is accessed
```

Typical cases:

```text
lazy loading
authorization
caching
remote invocation
logging
rate limiting
transactions
auditing
```

---

## 27. When Not to Use

Avoid when:

```text
direct access is already simple
no meaningful access policy exists
wrapper only adds confusion
```

---

## 28. Full Java 8 Virtual Proxy Example

### Subject

```java
public interface Image {

    void display();
}
```

### RealSubject

```java
public class RealImage
        implements Image {

    private final String fileName;

    public RealImage(String fileName) {

        this.fileName = fileName;

        loadFromDisk();
    }

    private void loadFromDisk() {

        System.out.println(
                "Loading image from disk: "
                + fileName
        );
    }

    @Override
    public void display() {

        System.out.println(
                "Displaying image: "
                + fileName
        );
    }
}
```

### Proxy

```java
public class ImageProxy
        implements Image {

    private final String fileName;

    private RealImage realImage;

    public ImageProxy(
            String fileName
    ) {
        this.fileName =
                fileName;
    }

    @Override
    public void display() {

        if (realImage == null) {

            realImage =
                    new RealImage(
                            fileName
                    );
        }

        realImage.display();
    }
}
```

### Client

```java
public class Client {

    public static void main(String[] args) {

        Image image =
                new ImageProxy(
                        "photo.jpg"
                );

        System.out.println(
                "Proxy created"
        );

        image.display();

        System.out.println(
                "Displaying again"
        );

        image.display();
    }
}
```

Expected flow:

```text
Proxy created

first display:
Loading image from disk
Displaying image

second display:
Displaying image
```

The expensive loading happens only once.

---

## 29. Common Interview Traps

### Trap 1
"Proxy and Decorator are the same."

No.

```text
Decorator = enhancement
Proxy = access control
```

### Trap 2
"Proxy must always create the real object."

No. It can receive an already-created target.

### Trap 3
"Proxy only means security."

No. Virtual, remote, caching, and monitoring proxies are also common.

### Trap 4
"JDK dynamic proxies can proxy any concrete class directly."

Classic JDK dynamic proxies are interface-oriented.

---

## 30. Interview Mental Model

If the interviewer says:

> "Create this expensive object only when needed."

Think:

```text
Virtual Proxy
```

If:

> "Check permission before forwarding."

Think:

```text
Protection Proxy
```

If:

> "Hide network access behind a local interface."

Think:

```text
Remote Proxy
```

If:

> "Check cache before invoking expensive service."

Think:

```text
Caching Proxy
```

Core sentence:

> Proxy provides a substitute for another object and controls access to that object while preserving the same client-facing abstraction.

---

## 31. Fast Revision Table

| Concept | Meaning |
|---|---|
| Subject | Shared client-facing abstraction |
| RealSubject | Actual implementation |
| Proxy | Stand-in controlling access |
| Virtual Proxy | Lazy creation/loading |
| Protection Proxy | Authorization |
| Remote Proxy | Local representative of remote service |
| Caching Proxy | Cache before delegation |
| Dynamic Proxy | Runtime-generated wrapper/interceptor |

---

## 32. One-Line Structural Memory Aid

```text
Adapter = compatibility

Decorator = behavior enhancement

Facade = simplification

Proxy = access control
```
