# Builder Pattern — Revision Notes

> **Primary language:** Java  
> **Go:** Included only as a short awareness/reference section.  
> **Category:** Creational Design Pattern

---

## 1. Intent

The **Builder Pattern** separates the construction of a complex object from the final object itself.

Use it when an object has:

- many constructor parameters,
- many optional fields,
- multiple valid configurations,
- validation rules during construction,
- a desire for immutability,
- or construction that is easier to understand as a sequence of named steps.

### Core mental model

```text
Factory
-------
Which object / implementation should I create?

Builder
-------
How should this complex object be constructed/configured?
```

---

## 2. The Problem Builder Solves

Assume we have this object:

```java
public class User {
    private String name;
    private String email;
    private int age;
    private String phone;
    private String address;
    private boolean admin;
}
```

A constructor can quickly become difficult to read:

```java
User user = new User(
        "Alice",
        "alice@example.com",
        25,
        "9999999999",
        "Delhi",
        true
);
```

As more fields are added, the call may look like:

```java
User user = new User(
        "Alice",
        "alice@example.com",
        25,
        "9999999999",
        "Delhi",
        true,
        false,
        true,
        false,
        "PREMIUM",
        null
);
```

The main problem is that the call site does not explain what values such as these mean:

```text
true, false, true, false
```

The caller has to inspect the constructor signature to understand the arguments.

---

## 3. Telescoping Constructor Problem

Without Builder, developers often create many overloaded constructors:

```java
public User(String name) { ... }

public User(String name, String email) { ... }

public User(String name, String email, int age) { ... }

public User(String name, String email, int age, String phone) { ... }
```

This produces the **Telescoping Constructor Problem**.

```text
User(name)

User(name, email)

User(name, email, age)

User(name, email, age, phone)

User(name, email, age, phone, address)

User(name, email, age, phone, address, admin)
```

Problems:

- too many constructor overloads,
- difficult maintenance,
- hard-to-read call sites,
- argument-order mistakes,
- optional parameters become awkward.

---

## 4. Builder Pattern Architecture

A common modern Java Builder looks like this:

```text
+------------------+
|      Client      |
+------------------+
         |
         | creates/configures
         v
+------------------+
|     Builder      |
+------------------+
| name             |
| email            |
| age              |
| phone            |
+------------------+
| name()           |
| email()          |
| age()            |
| phone()          |
| build()          |
+--------+---------+
         |
         | creates
         v
+------------------+
|      User        |
+------------------+
| name             |
| email            |
| age              |
| phone            |
+------------------+
```

### Construction lifecycle

```text
Create Builder
      |
      v
Configure field
      |
      v
Configure field
      |
      v
Configure field
      |
      v
Validate
      |
      v
   build()
      |
      v
Final Object
```

---

## 5. Relationship Between Product and Builder

In the common nested Java implementation:

```text
User
 |
 +---- static nested Builder
```

Conceptually:

```text
Builder --------creates--------> User
```

The builder **has the values required to construct** the product, but the final `User` does not need to keep a `Builder` reference.

So this is not normally a permanent runtime `has-a Builder` relationship inside `User`.

Instead, the important relationship is:

```text
Builder
  |
  | temporary construction dependency
  v
Product
```

The `Builder` exists to assemble the state and then pass it into the `User` constructor.

---

## 6. Java Implementation — Classic Nested Builder

```java
public class User {

    private final String name;
    private final String email;
    private final int age;
    private final String phone;
    private final String address;
    private final boolean admin;

    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.phone = builder.phone;
        this.address = builder.address;
        this.admin = builder.admin;
    }

    public static class Builder {

        private String name;
        private String email;
        private int age;
        private String phone;
        private String address;
        private boolean admin;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder admin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
```

### Usage

```java
User user = new User.Builder()
        .name("Alice")
        .email("alice@example.com")
        .age(25)
        .phone("9999999999")
        .address("Delhi")
        .admin(true)
        .build();
```

---

## 7. What Happens Internally?

Start with:

```java
new User.Builder()
```

The temporary builder initially contains Java default values:

```text
Builder
------------------------
name    = null
email   = null
age     = 0
phone   = null
address = null
admin   = false
```

After:

```java
.name("Alice")
```

state becomes:

```text
Builder
------------------------
name    = "Alice"
email   = null
age     = 0
...
```

After:

```java
.email("alice@example.com")
```

state becomes:

```text
Builder
------------------------
name    = "Alice"
email   = "alice@example.com"
age     = 0
...
```

Finally:

```java
.build()
```

executes:

```java
return new User(this);
```

Inside `Builder.build()`, `this` means the current `Builder` instance.

So:

```java
new User(this)
```

means:

> Construct a `User` using all values currently stored in this Builder.

---

## 8. Why Builder Methods Return `this`

Example:

```java
public Builder name(String name) {
    this.name = name;
    return this;
}
```

`return this` returns the same Builder object after changing its state.

This enables **method chaining**:

```java
new User.Builder()
        .name("Alice")
        .email("alice@example.com")
        .age(25)
        .build();
```

Without returning `this`:

```java
User.Builder builder = new User.Builder();

builder.name("Alice");
builder.email("alice@example.com");
builder.age(25);

User user = builder.build();
```

Both are valid, but returning `this` gives a **fluent API**.

### Important distinction

```java
this.name = name;
```

means:

```text
builder object's field = method argument
```

while:

```java
return this;
```

means:

```text
return the current Builder object
```

---

## 9. Why Is `Builder` Usually `static`?

Typical declaration:

```java
public static class Builder {
}
```

This is a very important Java-specific Builder question.

A **non-static inner class** requires an existing instance of its outer class.

Example:

```java
public class User {
    public class Builder {
    }
}
```

Creating it would require an existing `User`:

```java
User user = ...;
User.Builder builder = user.new Builder();
```

But the Builder's purpose is to create the `User` in the first place.

That would create a circular conceptual dependency:

```text
Need User
   |
   v
Create Builder
   |
   v
Create User
```

That makes no sense for object construction.

With a static nested class:

```java
public static class Builder {
}
```

we can directly do:

```java
User.Builder builder = new User.Builder();
```

No existing `User` object is required.

### Mental model

```text
User
 |
 +---- static Builder

Builder belongs to User's namespace,
but does not belong to a particular User instance.
```

### Important Java terminology

```java
static class Builder
```

inside another class is technically a **static nested class**, not a non-static inner class.

---

## 10. Why Is the Product Constructor `private`?

Example:

```java
private User(Builder builder) {
    ...
}
```

The goal is to force controlled construction through the Builder.

We want:

```java
User user = new User.Builder()
        .name("Alice")
        .build();
```

rather than allowing clients to bypass the Builder with a public constructor.

Architecture:

```text
Client
   |
   X-------> direct private User constructor
   |
   v
Builder
   |
   v
User
```

Because the nested `Builder` is declared inside `User`, it can access the enclosing class's private constructor.

---

## 11. Why Are Product Fields Often `final`?

Typical implementation:

```java
private final String name;
private final String email;
private final int age;
```

The Builder itself is mutable while construction is happening.

The final object can be immutable after `build()`.

```text
Builder
-------
mutable while configuring

        build()
           |
           v

User
----
immutable after construction
```

Benefits of immutability:

- fewer accidental state changes,
- easier reasoning,
- safer sharing,
- simpler concurrency behavior,
- stronger invariants.

Builder works particularly well when the product should be immutable.

---

## 12. Validation in `build()`

`build()` is a natural location to validate the final configuration.

```java
public User build() {

    if (name == null || name.isBlank()) {
        throw new IllegalStateException("Name is required");
    }

    if (email == null || email.isBlank()) {
        throw new IllegalStateException("Email is required");
    }

    if (age < 0) {
        throw new IllegalStateException("Age cannot be negative");
    }

    return new User(this);
}
```

Flow:

```text
Builder state
     |
     v
   build()
     |
     v
 Validation
   /     \
valid   invalid
 |         |
 v         v
User    Exception
```

The temporary Builder is allowed to be incomplete while fields are being configured.

The goal is that the final object is created only after its invariants are satisfied.

---

## 13. Required vs Optional Fields

Suppose:

```text
Required
--------
name
email

Optional
--------
age
phone
address
admin
```

One clean design is to require mandatory values in the Builder constructor:

```java
public static class Builder {

    private final String name;
    private final String email;

    private int age;
    private String phone;
    private String address;
    private boolean admin;

    public Builder(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Builder age(int age) {
        this.age = age;
        return this;
    }

    public Builder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public Builder address(String address) {
        this.address = address;
        return this;
    }

    public Builder admin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public User build() {
        return new User(this);
    }
}
```

Usage:

```java
User user = new User.Builder(
        "Alice",
        "alice@example.com"
)
        .age(25)
        .phone("9999999999")
        .build();
```

Mental model:

```text
Builder constructor
       |
       v
Required fields

Builder methods
       |
       v
Optional fields
```

Another valid approach is to keep a no-argument Builder and validate all mandatory fields inside `build()`.

The choice depends on API ergonomics and how strongly you want construction-time requirements expressed by the type/API.

---

## 14. Builder vs JavaBeans / Setters

JavaBeans style:

```java
User user = new User();

user.setName("Alice");
user.setEmail("alice@example.com");
user.setAge(25);
```

Main problem: the final object can exist in a partially initialized state.

After only:

```java
User user = new User();
user.setName("Alice");
```

we may have:

```text
User
------------------------
name  = Alice
email = null
age   = 0
```

The object already exists even though it may be invalid.

Builder changes where incomplete state lives:

```text
Builder
   |
   | incomplete state is allowed here
   |
   v
build()
   |
   v
Validated / complete User
```

### Comparison

| Approach | Main characteristic |
|---|---|
| Constructor | Simple for small, mandatory parameter sets |
| Setters / JavaBeans | Easy to mutate, but object may be partially initialized |
| Builder | Named construction steps; supports validation and immutable final objects |

---

## 15. Builder vs Factory Pattern

This distinction is important.

### Factory Pattern

Factory answers:

> **Which object / implementation should be created?**

Example:

```java
Notification notification =
        NotificationFactory.create("EMAIL");
```

Possible architecture:

```text
             NotificationFactory
                    |
          ----------------------
          |         |          |
          v         v          v
       Email       SMS        Push
```

The Factory chooses a concrete implementation.

### Builder Pattern

Builder answers:

> **How should this object be constructed/configured?**

Example:

```java
User user = new User.Builder()
        .name("Alice")
        .email("alice@example.com")
        .age(25)
        .build();
```

Architecture:

```text
Client
  |
  v
Builder
  |
  | configuration
  v
Product
```

### Revision rule

```text
Factory -> WHAT / WHICH object?

Builder -> HOW to construct/configure it?
```

---

## 16. Factory and Builder Can Work Together

They solve different problems, so they can be combined.

```text
Factory
   |
   | selects builder/type
   v
Builder
   |
   | configures
   v
Product
```

Example:

```java
CarBuilder builder = CarBuilderFactory.getBuilder(CarType.SUV);

Car car = builder
        .engine("V6")
        .color("Black")
        .sunroof(true)
        .build();
```

Interpretation:

```text
Factory
-> Which builder / product family?

Builder
-> How should that product be configured?
```

---

## 17. Realistic Java Example

Without Builder:

```java
HttpRequest request = new HttpRequest(
        "https://example.com",
        "POST",
        true,
        5000,
        3,
        "application/json",
        null,
        true
);
```

The parameter meaning is unclear.

With Builder:

```java
HttpRequest request = new HttpRequest.Builder()
        .url("https://example.com")
        .method("POST")
        .timeout(5000)
        .retryCount(3)
        .contentType("application/json")
        .loggingEnabled(true)
        .build();
```

The call site becomes self-documenting.

---

## 18. Classic GoF Builder Structure

The original Gang of Four form can contain more participants:

```text
+----------+
|  Client  |
+----+-----+
     |
     v
+----------+
| Director |
+----+-----+
     |
     | construction sequence
     v
+----------+       implemented by       +-----------------+
| Builder  | <-------------------------- | ConcreteBuilder |
+----------+                              +--------+--------+
                                                  |
                                                  v
                                              +---------+
                                              | Product |
                                              +---------+
```

### Participants

#### Product
The complex object being created.

Example:

```text
Computer
User
HttpRequest
House
```

#### Builder
Defines construction operations.

#### ConcreteBuilder
Implements the construction operations and stores intermediate state.

#### Director
Knows which sequence/configuration of build steps to execute.

#### Client
Chooses/creates the builder and requests the desired construction.

---

## 19. What Is a Director?

A Director captures a predefined construction recipe.

Example:

```java
public class ComputerDirector {

    public Computer buildGamingComputer() {
        return new Computer.Builder()
                .ram(32)
                .storage(2000)
                .graphicsCard("RTX")
                .build();
    }
}
```

Usage:

```java
ComputerDirector director = new ComputerDirector();
Computer computer = director.buildGamingComputer();
```

Mental model:

```text
Builder
= knows HOW individual construction steps work

Director
= knows WHICH steps/configuration form a particular recipe
```

### Important modern Java note

Many real Java Builders do **not** use a Director.

The common version is simply:

```text
Client -> Builder -> Product
```

Do not add a Director unless repeated named construction recipes actually provide value.

---

## 20. Advantages

### 1. Readability

Instead of:

```java
new User("Alice", "a@x.com", 25, true, false);
```

use:

```java
new User.Builder()
        .name("Alice")
        .email("a@x.com")
        .age(25)
        .admin(true)
        .build();
```

### 2. Handles optional parameters well

No need to create many overloaded constructors.

### 3. Supports immutable products

Builder can be mutable while the resulting product has `final` fields and no setters.

### 4. Centralized validation

`build()` can validate cross-field invariants before constructing the product.

### 5. Fluent API

Named methods make construction easier to read and reduce positional argument mistakes.

### 6. Separates construction from representation

Complex construction logic does not need to pollute the client's code.

---

## 21. Disadvantages

### 1. More code

Often duplicates fields:

```text
Product fields
+
Builder fields
+
Builder methods
+
build()
```

### 2. Unnecessary for simple objects

For:

```java
new Point(10, 20);
```

this would be over-engineering:

```java
new Point.Builder()
        .x(10)
        .y(20)
        .build();
```

### 3. Builder itself is usually mutable

A builder should normally be treated as a construction helper, not shared carelessly across threads.

### 4. Validation can become complicated

If the Builder allows arbitrary call orders and combinations, invalid intermediate combinations may need careful validation at `build()` time.

---

## 22. When to Use Builder

Strong signals:

```java
new Something(
        a,
        b,
        c,
        d,
        e,
        f,
        g,
        h
);
```

Especially when parameters include repeated primitive types:

```text
boolean
boolean
String
int
boolean
```

Use Builder when you have several of these conditions:

- many optional fields,
- many constructor arguments,
- several values of the same type,
- immutable output desired,
- validation before construction,
- multiple construction configurations,
- multi-step construction,
- API readability matters.

---

## 23. When NOT to Use Builder

Prefer a normal constructor when the object is small and its required state is obvious.

Example:

```java
public record Point(int x, int y) {}
```

Usage:

```java
Point point = new Point(10, 20);
```

No Builder is needed.

Avoid adding Builder only because it is a known design pattern.

Use it when it reduces real construction complexity.

---

## 24. Common Builder Pitfalls

### Pitfall 1: Builder does no meaningful work

If a two-field value object gets a 30-line Builder, complexity has increased rather than decreased.

### Pitfall 2: Required fields are silently forgotten

Example:

```java
User user = new User.Builder()
        .age(25)
        .build();
```

If `name` and `email` are required, `build()` should validate them or the API should require them earlier.

### Pitfall 3: Building a mutable product anyway

If the final object has public setters for everything, some Builder benefits disappear.

Builder does not automatically imply immutability.

### Pitfall 4: Reusing a mutable Builder unexpectedly

Example:

```java
User.Builder builder = new User.Builder()
        .name("Alice");

User first = builder.email("a@example.com").build();
User second = builder.email("b@example.com").build();
```

This may be intentional, but remember that the Builder retains mutable state between calls.

### Pitfall 5: Confusing Builder with Factory

```text
Factory = object selection/creation abstraction
Builder = stepwise/configurable construction
```

### Pitfall 6: Adding a Director automatically

The GoF form describes a Director, but most simple Java Builders do not need one.

---

## 25. Immutable Builder Example With Validation

```java
public final class User {

    private final String name;
    private final String email;
    private final int age;
    private final String phone;
    private final boolean admin;

    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.phone = builder.phone;
        this.admin = builder.admin;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAdmin() {
        return admin;
    }

    public static class Builder {

        private final String name;
        private final String email;

        private int age;
        private String phone;
        private boolean admin;

        public Builder(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder admin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public User build() {
            validate();
            return new User(this);
        }

        private void validate() {
            if (name == null || name.isBlank()) {
                throw new IllegalStateException("name is required");
            }

            if (email == null || email.isBlank()) {
                throw new IllegalStateException("email is required");
            }

            if (age < 0) {
                throw new IllegalStateException("age cannot be negative");
            }
        }
    }
}
```

Usage:

```java
User user = new User.Builder(
        "Alice",
        "alice@example.com"
)
        .age(25)
        .phone("9999999999")
        .admin(true)
        .build();
```

---

## 26. Alternative API: `builder()` Factory Method

Some Java APIs prefer:

```java
User user = User.builder()
        .name("Alice")
        .email("alice@example.com")
        .build();
```

Implementation:

```java
public static Builder builder() {
    return new Builder();
}
```

This is mostly an API ergonomics choice.

Compare:

```java
new User.Builder()
```

with:

```java
User.builder()
```

Both can implement the same Builder Pattern.

---

## 27. Lombok Awareness

In Java projects using Lombok, you may see:

```java
@Builder
public class User {
    private String name;
    private String email;
    private int age;
}
```

Usage:

```java
User user = User.builder()
        .name("Alice")
        .email("alice@example.com")
        .age(25)
        .build();
```

Lombok generates much of the Builder boilerplate.

### Interview point

Know the pattern without Lombok.

`@Builder` is a code-generation convenience; Lombok is not the Builder Pattern itself.

---

## 28. Builder and Java Records

For very small immutable data carriers, a Java record may already make construction concise:

```java
public record User(String name, String email, int age) {}
```

Usage:

```java
User user = new User("Alice", "alice@example.com", 25);
```

A Builder may still help when:

- there are many components,
- many are optional,
- construction requires validation,
- positional arguments become hard to read.

Do not automatically add a Builder to every record.

---

## 29. Builder and Thread Safety

The resulting immutable product can be naturally safe to share provided the objects referenced by its fields are also handled appropriately.

The Builder itself is usually mutable:

```java
builder.name("Alice");
builder.email("alice@example.com");
```

Therefore:

```text
Builder
-> usually NOT intended for concurrent mutation

Built immutable Product
-> much easier to share safely
```

Builder Pattern itself does not automatically make every referenced object deeply immutable or thread-safe.

---

## 30. Interview Questions and Answers

### Q1. What problem does Builder Pattern solve?

It simplifies construction of complex objects, especially when they have many optional parameters, validation rules, or multiple configuration steps.

---

### Q2. Why not use multiple constructors?

Too many overloaded constructors lead to the **Telescoping Constructor Problem**, where APIs become difficult to read, maintain, and call correctly.

---

### Q3. Why is the nested Builder usually `static` in Java?

A non-static inner class requires an instance of the outer class. The Builder is supposed to create that outer object, so it should be constructible independently. A static nested Builder does not require an existing product instance.

---

### Q4. Why does each Builder method return `this`?

To return the same Builder instance and enable fluent method chaining.

```java
builder.name("Alice")
       .email("alice@example.com")
       .age(25);
```

---

### Q5. Why is the product constructor often `private`?

To prevent clients from bypassing the intended construction process and to centralize creation through the Builder.

---

### Q6. Why are product fields often `final`?

Builder is commonly used to create immutable objects. Fields are established during construction and cannot later be reassigned.

---

### Q7. Is Builder Pattern only for immutable objects?

No. Builder can construct mutable objects too. Immutability is a common complementary design choice, not a requirement of the pattern.

---

### Q8. Where should validation happen?

Often inside `build()`, because it is the point at which incomplete Builder state becomes a final object.

---

### Q9. Builder vs Factory?

```text
Factory -> chooses which object/implementation to create.
Builder -> controls how a complex object is configured and constructed.
```

---

### Q10. Can Factory and Builder be used together?

Yes. A Factory may select the appropriate Builder, after which the Builder configures and creates the product.

---

### Q11. What is a Director?

A Director encapsulates a particular sequence/recipe of construction steps. It is part of the classic GoF structure but is often omitted in modern Java Builder implementations.

---

### Q12. Does Builder guarantee immutability?

No. The final class must itself be designed to be immutable, for example with private final fields, no mutating setters, and careful handling of mutable referenced objects.

---

### Q13. Is `User.Builder` a separate object from `User`?

Yes. The Builder is a separate temporary object that stores construction state. `build()` then creates the final `User`.

---

### Q14. Does `User` have-a `Builder`?

Usually no, not in the permanent object-relationship sense. The Builder temporarily knows the data needed to construct `User`; the final `User` usually does not keep a Builder field.

---

### Q15. When is Builder overkill?

When the object has only a few obvious mandatory fields and a normal constructor is already clear and safe.

---

## 31. Fast Revision Diagram

```text
              BUILDER PATTERN

                  Client
                    |
                    v
          +-------------------+
          |      Builder      |
          +-------------------+
          | temporary state   |
          +-------------------+
          | name(...)         |
          | email(...)        |
          | age(...)          |
          | build()           |
          +---------+---------+
                    |
                    | validates + constructs
                    v
          +-------------------+
          |      Product      |
          +-------------------+
          | final state       |
          | preferably        |
          | immutable         |
          +-------------------+
```

### Java nested form

```text
+-----------------------------------+
| User                              |
|-----------------------------------|
| - final name                      |
| - final email                     |
| - final age                       |
|                                   |
| - User(Builder builder)           |
|                                   |
| + static class Builder            |
|     - name                        |
|     - email                       |
|     - age                         |
|                                   |
|     + name(...) : Builder         |
|     + email(...) : Builder        |
|     + age(...) : Builder          |
|     + build() : User              |
+-----------------------------------+
```

---

## 32. Fast Revision Table

| Concept | Remember |
|---|---|
| Pattern type | Creational |
| Main purpose | Construct complex/configurable objects clearly |
| Main problem | Telescoping constructors / unreadable argument lists |
| Builder state | Usually mutable |
| Product state | Often immutable |
| `return this` | Enables fluent chaining |
| `static Builder` | Builder does not require an existing Product instance |
| private Product constructor | Forces controlled construction |
| `build()` | Validation + final construction |
| Factory | Which object? |
| Builder | How to construct it? |
| Director | Optional recipe/orchestration of build steps |

---

## 33. One-Minute Interview Answer

> The Builder Pattern is a creational design pattern used when constructing an object requires many parameters, optional properties, validation, or multiple configuration steps. Instead of exposing a large constructor or many overloaded constructors, a mutable Builder collects configuration through named fluent methods and creates the final object through `build()`. In Java, the Builder is commonly a `static` nested class so it can be created without an existing instance of the product. The product constructor is often private and its fields final, allowing the Builder to create an immutable object after validating its state. Factory and Builder solve different problems: Factory decides which object to create, while Builder controls how an object is constructed.

---

# Go Awareness Appendix

> Java is the primary language for this pattern. This section is only for cross-language awareness.

Go often uses either an explicit builder or, more idiomatically for configuration-heavy APIs, the **functional options pattern**.

## Explicit Go-style Builder

```go
package main

import "fmt"

type User struct {
    Name    string
    Email   string
    Age     int
    Phone   string
    IsAdmin bool
}

type UserBuilder struct {
    name    string
    email   string
    age     int
    phone   string
    isAdmin bool
}

func NewUserBuilder() *UserBuilder {
    return &UserBuilder{}
}

func (b *UserBuilder) Name(name string) *UserBuilder {
    b.name = name
    return b
}

func (b *UserBuilder) Email(email string) *UserBuilder {
    b.email = email
    return b
}

func (b *UserBuilder) Age(age int) *UserBuilder {
    b.age = age
    return b
}

func (b *UserBuilder) Build() (*User, error) {
    if b.name == "" {
        return nil, fmt.Errorf("name is required")
    }

    return &User{
        Name:    b.name,
        Email:   b.email,
        Age:     b.age,
        Phone:   b.phone,
        IsAdmin: b.isAdmin,
    }, nil
}
```

Usage:

```go
user, err := NewUserBuilder().
    Name("Alice").
    Email("alice@example.com").
    Age(25).
    Build()
```

## Go Functional Options Awareness

Go frequently represents similar configuration with functional options:

```go
server := NewServer(
    WithPort(8080),
    WithTLS(true),
    WithTimeout(30),
)
```

This serves a similar readability/configuration goal but is not identical to the classic GoF Builder implementation.

---

# Final Revision Summary

```text
Problem:
Huge / telescoping constructors and complex object construction.

Solution:
Use a separate Builder to accumulate configuration and construct the Product.

Java structure:
Product
  |
  +-- private constructor
  +-- final fields
  +-- static nested Builder
         |
         +-- mutable temporary fields
         +-- fluent methods returning this
         +-- build()

Key relationship:
Builder ----creates----> Product

Important:
The final Product normally does NOT permanently contain the Builder.

Factory vs Builder:
Factory = which object?
Builder = how to construct it?

Use when:
Many parameters + optional fields + readability + validation + immutability.

Avoid when:
A small clear constructor already solves the problem.
```
