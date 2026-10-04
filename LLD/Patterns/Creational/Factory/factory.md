# Factory Pattern — Revision Notes

## 1. What is the Factory Pattern?

The **Factory Pattern** is a **creational design pattern**.

It is used when object creation should be separated from the client code.

Instead of directly creating a concrete implementation:

```java
Notification notification = new EmailNotification();
```

we delegate the creation to a factory:

```java
Notification notification =
    NotificationFactory.createNotification("email");
```

The client depends mainly on the abstraction (`Notification`) instead of a concrete implementation (`EmailNotification`).

---

## 2. Core Idea

> The client asks the factory for an object and does not need to know the exact object-creation logic.

### Without Factory

```text
Client
  |
  v
new EmailNotification()
```

The client directly knows about the concrete class.

### With Factory

```text
                +----------------------+
                |        Client        |
                +----------+-----------+
                           |
                           | asks for Notification
                           v
                +----------------------+
                | NotificationFactory  |
                +----------+-----------+
                           |
                 creates   |
              +------------+------------+
              |                         |
              v                         v
 +------------------------+   +------------------------+
 |   EmailNotification    |   |    SMSNotification     |
 +-----------+------------+   +-----------+------------+
             |                            |
             +-------------+--------------+
                           |
                           | implements
                           v
                +----------------------+
                |     Notification     |
                |      interface       |
                +----------------------+
```

---

## 3. Important Relationships in the Diagram

### `EmailNotification IS-A Notification`

`EmailNotification` implements `Notification`.

```java
class EmailNotification implements Notification
```

Therefore:

```text
EmailNotification IS-A Notification
SMSNotification   IS-A Notification
```

This is an **IS-A relationship**.

---

### Does `NotificationFactory` have a HAS-A relationship with `Notification`?

Usually, **no**.

A `HAS-A` relationship normally means one object stores another object as a field/member.

Example:

```java
class Car {
    Engine engine;
}
```

Here:

```text
Car HAS-A Engine
```

because `Engine` is stored inside `Car`.

Our factory does not normally store a `Notification`:

```java
class NotificationFactory {

    public static Notification createNotification(String type) {
        return new EmailNotification();
    }
}
```

The factory only **creates and returns** a `Notification`.

So the more accurate relationship is:

```text
NotificationFactory ----creates----> Notification
```

or:

```text
NotificationFactory ----depends on----> Notification
```

This is generally called a **dependency / uses relationship**, not composition.

### Revision shortcut

```text
EmailNotification  IS-A  Notification
SMSNotification    IS-A  Notification

NotificationFactory USES / CREATES Notification
```

---

## 4. Java Implementation

### Interface

```java
interface Notification {
    void send();
}
```

### Concrete Implementations

```java
class EmailNotification implements Notification {

    @Override
    public void send() {
        System.out.println("Sending Email");
    }
}
```

```java
class SMSNotification implements Notification {

    @Override
    public void send() {
        System.out.println("Sending SMS");
    }
}
```

### Factory

```java
final class NotificationFactory {

    private NotificationFactory() {
        // Prevent creating factory objects
    }

    public static Notification createNotification(String type) {

        if (type.equalsIgnoreCase("email")) {
            return new EmailNotification();
        }

        if (type.equalsIgnoreCase("sms")) {
            return new SMSNotification();
        }

        throw new IllegalArgumentException(
            "Invalid notification type"
        );
    }
}
```

### Client

```java
public class Main {

    public static void main(String[] args) {

        Notification notification =
            NotificationFactory.createNotification("email");

        notification.send();
    }
}
```

Output:

```text
Sending Email
```

---

## 5. Why is `NotificationFactory` not declared `static` in Java?

A **top-level Java class cannot be declared `static`**.

Invalid:

```java
static class NotificationFactory {
}
```

if `NotificationFactory` is a top-level class.

However, its method can be static:

```java
public static Notification createNotification(String type)
```

Therefore we can call:

```java
NotificationFactory.createNotification("email");
```

without doing:

```java
NotificationFactory factory = new NotificationFactory();
```

---

## 6. When can a Java class be `static`?

A class can be declared `static` when it is a **nested class**.

Example:

```java
class App {

    static class NotificationFactory {

        static Notification createNotification(String type) {
            return new EmailNotification();
        }
    }
}
```

Revision rule:

```text
Top-level class  -> cannot be static
Nested class     -> can be static
Method           -> can be static
```

---

## 7. Why make the Factory constructor private?

If every factory operation is static, creating an object of the factory is unnecessary.

Without a private constructor:

```java
NotificationFactory factory = new NotificationFactory();
```

could still be allowed.

To prevent this:

```java
private NotificationFactory() {
}
```

Typical utility-style factory:

```java
final class NotificationFactory {

    private NotificationFactory() {
    }

    public static Notification createNotification(String type) {
        // creation logic
    }
}
```

---

# Factory Pattern in Go

Go does not have classes in the same way Java does.

The equivalent design normally uses:

- an `interface`
- concrete `struct` types
- a factory function

---

## 8. Go Interface

```go
package main

import "fmt"

type Notification interface {
    Send()
}
```

---

## 9. Concrete Implementations

### Email

```go
type EmailNotification struct{}

func (EmailNotification) Send() {
    fmt.Println("Sending Email")
}
```

### SMS

```go
type SMSNotification struct{}

func (SMSNotification) Send() {
    fmt.Println("Sending SMS")
}
```

In Go, interfaces are implemented **implicitly**.

We do not write something like:

```text
implements Notification
```

If a type provides the required methods, it satisfies the interface.

Since both types provide:

```go
Send()
```

they satisfy:

```go
Notification
```

---

## 10. Factory Function in Go

```go
func NewNotification(notificationType string) (Notification, error) {

    switch notificationType {

    case "email":
        return EmailNotification{}, nil

    case "sms":
        return SMSNotification{}, nil

    default:
        return nil, fmt.Errorf(
            "invalid notification type: %s",
            notificationType,
        )
    }
}
```

---

## 11. Complete Go Example

```go
package main

import "fmt"

type Notification interface {
    Send()
}

type EmailNotification struct{}

func (EmailNotification) Send() {
    fmt.Println("Sending Email")
}

type SMSNotification struct{}

func (SMSNotification) Send() {
    fmt.Println("Sending SMS")
}

func NewNotification(notificationType string) (Notification, error) {

    switch notificationType {

    case "email":
        return EmailNotification{}, nil

    case "sms":
        return SMSNotification{}, nil

    default:
        return nil, fmt.Errorf(
            "invalid notification type: %s",
            notificationType,
        )
    }
}

func main() {

    notification, err := NewNotification("email")

    if err != nil {
        fmt.Println(err)
        return
    }

    notification.Send()
}
```

Output:

```text
Sending Email
```

---

## 12. Java vs Go

| Java | Go |
|---|---|
| `interface Notification` | `type Notification interface` |
| Class | Struct |
| `implements Notification` | Implemented implicitly |
| Static factory method | Package-level factory function |
| `NotificationFactory.createNotification()` | `NewNotification()` |
| Exceptions often used for errors | Explicit `error` return value |

---

## 13. Go Architecture

```text
                   +------------------+
                   |      Client      |
                   +--------+---------+
                            |
                            | calls
                            v
                   +------------------+
                   | NewNotification  |
                   | factory function |
                   +--------+---------+
                            |
                    creates |
                 +----------+----------+
                 |                     |
                 v                     v
       +-------------------+  +-------------------+
       | EmailNotification |  |  SMSNotification  |
       +---------+---------+  +---------+---------+
                 |                     |
                 +----------+----------+
                            |
                            | satisfies
                            v
                   +------------------+
                   |   Notification   |
                   |    interface     |
                   +------------------+
```

---

## 14. Why Go usually uses a Factory Function instead of a Factory Class

Go does not require creating a class such as:

```text
NotificationFactory
```

just to hold one creation method.

A package-level function is usually enough:

```go
NewNotification("email")
```

This follows common Go style.

Naming constructor/factory functions with `New...` is also a common Go convention.

Examples:

```go
NewNotification(...)
NewServer(...)
NewClient(...)
NewRepository(...)
```

---

## 15. Advantages of the Factory Pattern

### Loose Coupling

Client code depends on the interface:

```java
Notification
```

instead of:

```java
EmailNotification
```

### Centralized Object Creation

Creation logic exists in one place.

### Easier Extension

New implementations can be introduced:

```text
PushNotification
WhatsAppNotification
SlackNotification
```

### Cleaner Client Code

The client only asks:

```text
Give me a notification implementation.
```

It does not need to understand construction details.

---

## 16. Potential Drawback

A simple factory using a large `if` or `switch` can become difficult to maintain:

```text
email
sms
push
slack
whatsapp
telegram
...
```

As the application grows, other approaches may be useful:

- Factory Method
- Abstract Factory
- Dependency Injection
- Registry-based factories

---

# Final Revision Cheat Sheet

```text
FACTORY PATTERN
===============

Type:
    Creational Design Pattern

Purpose:
    Centralize object creation.

Main idea:
    Client asks factory for an object.

Client should depend on:
    Interface / abstraction

Java:
    NotificationFactory.createNotification("email")

Go:
    NewNotification("email")

Relationships:
    EmailNotification IS-A Notification
    SMSNotification   IS-A Notification

    NotificationFactory USES / CREATES Notification

    Factory -> Notification is normally NOT a HAS-A
    relationship unless the factory actually stores a
    Notification object as a field.

Java static rule:
    Top-level class -> cannot be static
    Nested class    -> can be static
    Method          -> can be static

Go:
    No factory class is required.
    A factory function is normally sufficient.

Main benefits:
    - loose coupling
    - centralized creation
    - cleaner client code
    - easier replacement of implementations
```
