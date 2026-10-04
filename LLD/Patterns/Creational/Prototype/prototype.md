# Prototype Pattern — Revision Notes

> **Primary language:** Java  
> **Go:** Included only as a short awareness/reference section.  
> **Category:** Creational Design Pattern

---

## 1. Intent

The **Prototype Pattern** creates new objects by copying an existing, already configured object instead of rebuilding the object from scratch.

Use it when:

- object construction/configuration is expensive,
- many similar objects are required,
- a runtime template already exists,
- construction logic should be hidden from the client,
- or the system needs independent copies of configured object graphs.

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
I already have an object.
Create another object like this one.
```

---

## 2. The Problem Prototype Solves

Suppose a game character requires substantial configuration:

```java
public class GameCharacter {
    private String type;
    private int health;
    private int attack;
    private int defense;
    private String weapon;
    private String armor;
}
```

Creating every warrior manually duplicates configuration:

```java
GameCharacter warrior1 = new GameCharacter();
warrior1.setType("Warrior");
warrior1.setHealth(100);
warrior1.setAttack(80);
warrior1.setDefense(70);
warrior1.setWeapon("Sword");
warrior1.setArmor("Heavy Armor");

GameCharacter warrior2 = new GameCharacter();
warrior2.setType("Warrior");
warrior2.setHealth(100);
warrior2.setAttack(80);
warrior2.setDefense(70);
warrior2.setWeapon("Sword");
warrior2.setArmor("Heavy Armor");
```

Prototype instead lets us configure once and copy:

```java
GameCharacter warrior2 = warrior1.copy();
```

---

## 3. Reference Assignment Is NOT Object Copying

This is not Prototype:

```java
GameCharacter warrior2 = warrior1;
```

Both references point to the same object:

```text
warrior1 --------+
                 |
                 v
          +---------------+
          | Character #1  |
          +---------------+
                 ^
                 |
warrior2 --------+
```

So changing through either reference changes the same object.

A real copy creates another object:

```text
warrior1                   warrior2
   |                          |
   v                          v
+-------------+            +-------------+
| Object #1   |            | Object #2   |
| HP = 100    |            | HP = 100    |
+-------------+            +-------------+
```

The state may initially be equivalent, but object identity differs.

```java
System.out.println(warrior1 == warrior2); // false
```

---

## 4. Prototype Pattern Architecture

The classic GoF structure is:

```text
                 Prototype
              +-------------+
              | copy()/clone|
              +------+------+ 
                     ^
                     |
          +----------+----------+
          |                     |
+------------------+   +------------------+
| ConcreteProto A  |   | ConcreteProto B  |
+------------------+   +------------------+
| copy()           |   | copy()           |
+------------------+   +------------------+
```

Client flow:

```text
Client
  |
  | asks existing object to copy itself
  v
Prototype instance
  |
  | copy()
  v
New independent object
```

---

## 5. Relationship Between Prototype and Copy

The important relationship is temporal:

```text
Existing Object
      |
      | copy/clone
      v
New Object
```

The copied object does not normally need to retain a reference to the original object.

```text
Before copy:
prototype ---> Object #1

After copy:
prototype ---> Object #1
copy      ---> Object #2
```

Both objects can then evolve independently, depending on whether nested state was deeply copied.

---

## 6. Java Implementation — Explicit `copy()` Method

A straightforward modern Java implementation is:

```java
public class GameCharacter {

    private String type;
    private int health;
    private int attack;
    private int defense;

    public GameCharacter(
            String type,
            int health,
            int attack,
            int defense
    ) {
        this.type = type;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
    }

    public GameCharacter copy() {
        return new GameCharacter(
                this.type,
                this.health,
                this.attack,
                this.defense
        );
    }
}
```

Client:

```java
GameCharacter warrior1 =
        new GameCharacter("Warrior", 100, 80, 70);

GameCharacter warrior2 = warrior1.copy();
```

This is Prototype even though Java's `Cloneable` interface is not involved.

---

## 7. Copy Constructor

A very explicit Java technique is a copy constructor:

```java
public class GameCharacter {

    private String type;
    private int health;
    private int attack;
    private int defense;

    public GameCharacter(String type, int health, int attack, int defense) {
        this.type = type;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
    }

    public GameCharacter(GameCharacter other) {
        this.type = other.type;
        this.health = other.health;
        this.attack = other.attack;
        this.defense = other.defense;
    }
}
```

Client:

```java
GameCharacter warrior2 = new GameCharacter(warrior1);
```

A public `copy()` method can internally delegate to the copy constructor:

```java
public GameCharacter copy() {
    return new GameCharacter(this);
}
```

This keeps the copying API expressive while centralizing copy logic.

---

## 8. Why Not Just Call the Normal Constructor Again?

For a trivial object, calling the constructor again may be perfectly fine.

Prototype becomes useful when the source object is already richly configured:

```text
Configured Character
├── Stats
├── Weapon configuration
├── Armor configuration
├── Skill tree
├── Appearance
├── AI configuration
└── Inventory
```

Rebuilding all of this every time can duplicate logic and configuration.

Prototype reuses an established template:

```text
Fully configured template
          |
          | copy()
          v
New configured instance
```

---

## 9. Shallow Copy

A **shallow copy** creates a new top-level object but copies references to nested mutable objects.

Example:

```java
public class User {
    private String name;
    private Address address;

    public User copy() {
        User copy = new User();
        copy.name = this.name;
        copy.address = this.address;
        return copy;
    }
}
```

Memory:

```text
user1                         user2
+-------------+              +-------------+
| name=Alice  |              | name=Alice  |
| address ----+----+    +----+ address     |
+-------------+    |    |    +-------------+
                   |    |
                   v    v
                +-----------+
                | Address   |
                | Delhi     |
                +-----------+
```

The two `User` objects are different, but they share one `Address`.

---

## 10. Deep Copy

A **deep copy** creates independent copies of mutable nested state that should not be shared.

```java
public User copy() {
    User copy = new User();
    copy.name = this.name;
    copy.address = new Address(
            this.address.getCity(),
            this.address.getCountry()
    );
    return copy;
}
```

Memory:

```text
user1
+-------------+
| address ----+----> +-----------+
+-------------+      | Address#1 |
                     | Delhi     |
                     +-----------+

user2
+-------------+
| address ----+----> +-----------+
+-------------+      | Address#2 |
                     | Delhi     |
                     +-----------+
```

Now changing `user2.address` does not affect `user1.address`.

---

## 11. Shallow vs Deep Copy — Core Interview Difference

```text
SHALLOW COPY
============
Top-level object       -> new
Nested mutable objects -> shared

DEEP COPY
=========
Top-level object       -> new
Nested mutable objects -> copied where independent ownership is required
```

Important interview wording:

> Deep copy does not mean blindly cloning every reachable object. It means copying the mutable state that must be independently owned. Immutable state can usually be safely shared.

---

## 12. Primitive, Immutable, and Mutable Fields

Consider:

```java
private int age;
private boolean admin;
private String name;
private LocalDate birthDate;
private Address address;
private List<Role> roles;
```

### Normally safe to reuse/copy directly

```text
int / boolean / other primitives
String
LocalDate
other immutable value objects
```

### Requires ownership decision

```text
Address
ArrayList
HashMap
HashSet
mutable custom objects
arrays
```

The question is not simply "reference or primitive?" The better question is:

> Can this referenced object's state change, and should the original and copy observe the same changes?

---

## 13. Collections and the Deep Copy Trap

Suppose:

```java
private List<Role> roles;
```

This:

```java
this.roles = new ArrayList<>(other.roles);
```

creates a new list but still shares each `Role` object.

```text
user1.roles ---> List #1 ---+
                           +----> Role A
                           +----> Role B
user2.roles ---> List #2 ---+
```

If `Role` is mutable and independent ownership is required, copy each role:

```java
this.roles = other.roles.stream()
        .map(Role::new)
        .collect(Collectors.toCollection(ArrayList::new));
```

Now:

```text
User #1 -> List #1 -> Role #1A, Role #1B
User #2 -> List #2 -> Role #2A, Role #2B
```

---

## 14. Deep Copy and Cyclic Object Graphs

Deep copying becomes harder when objects reference each other.

Example:

```text
Person A ---> Person B
   ^            |
   |            |
   +------------+
```

A naive recursive copy can recurse forever.

For general graph copying, maintain an identity map:

```text
original object -> copied object
```

Before copying a node:

1. check whether it was already copied,
2. reuse the existing copied node if so,
3. otherwise create/register the copy,
4. then recursively copy outgoing references.

This preserves cycles and shared topology.

For ordinary domain objects, avoid building a generic deep-copy engine unless the problem actually requires it.

---

## 15. Java `Cloneable` and `Object.clone()`

Java historically supports cloning using `Cloneable`:

```java
public class User implements Cloneable {

    private String name;
    private int age;

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
```

Client:

```java
User user2 = user1.clone();
```

Key point:

```java
super.clone()
```

performs a field-by-field **shallow copy**.

For nested mutable state, explicit copying is still required.

---

## 16. Why `Cloneable` Is Often Avoided in Modern Java

`Cloneable` has awkward semantics:

- the interface itself does not declare `clone()`,
- `Object.clone()` is `protected`,
- default behavior is shallow,
- constructors are bypassed by cloning mechanics,
- checked `CloneNotSupportedException` historically complicates usage,
- deep-copy ownership remains manual.

Because of this, many codebases prefer:

```text
copy constructor
explicit copy() method
factory method
immutable value objects
```

Prototype is a design pattern; it does not require Java's `Cloneable` mechanism.

---

## 17. Deep Copy With `clone()`

If `Address` is also cloneable:

```java
public class User implements Cloneable {

    private String name;
    private Address address;

    @Override
    public User clone() {
        try {
            User copy = (User) super.clone();
            copy.address = this.address.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
```

This demonstrates the key rule:

```text
super.clone()
   |
   v
shallow top-level copy
   |
   v
manually copy nested mutable state
```

---

## 18. Prototype Interface

For multiple prototype types, introduce a contract:

```java
public interface Prototype<T> {
    T copy();
}
```

Example:

```java
public class Document implements Prototype<Document> {

    private String title;
    private String content;

    @Override
    public Document copy() {
        return new Document(this);
    }

    private Document(Document other) {
        this.title = other.title;
        this.content = other.content;
    }
}
```

This communicates that the object supports copying without exposing construction details.

---

## 19. Prototype Registry

A **Prototype Registry** stores preconfigured prototype instances.

```text
Prototype Registry
+----------------------------+
| WARRIOR -> Warrior template|
| MAGE    -> Mage template   |
| ARCHER  -> Archer template |
+-------------+--------------+
              |
              | lookup + copy
              v
         New instance
```

Example:

```java
public class CharacterRegistry {

    private final Map<String, GameCharacter> prototypes = new HashMap<>();

    public void register(String key, GameCharacter prototype) {
        prototypes.put(key, prototype);
    }

    public GameCharacter create(String key) {
        GameCharacter prototype = prototypes.get(key);

        if (prototype == null) {
            throw new IllegalArgumentException("Unknown prototype: " + key);
        }

        return prototype.copy();
    }
}
```

Usage:

```java
registry.register("WARRIOR", warriorTemplate);

GameCharacter warrior1 = registry.create("WARRIOR");
GameCharacter warrior2 = registry.create("WARRIOR");
```

---

## 20. Prototype Registry vs Factory

They may look similar at the call site:

```java
registry.create("WARRIOR");
factory.create("WARRIOR");
```

But internally they differ:

```text
Factory
------
usually chooses construction logic/type
and creates from scratch

Prototype Registry
------------------
finds an existing configured prototype
and copies it
```

They can also be combined.

---

## 21. Prototype vs Builder Pattern

### Builder

```java
User user = new User.Builder()
        .name("Alice")
        .email("alice@example.com")
        .age(25)
        .build();
```

```text
Nothing
  |
Builder
  |
configure step by step
  |
build()
  v
Object
```

### Prototype

```java
User user2 = user1.copy();
```

```text
Existing configured object
          |
          | copy()
          v
      New object
```

### Mental model

```text
Builder
→ construct/configure step by step

Prototype
→ copy an already configured object
```

---

## 22. Prototype vs Factory Pattern

```text
Factory
→ Which concrete object/type should be created?

Builder
→ How should a complex object be configured and constructed?

Prototype
→ Which existing configured object should be copied?
```

Prototype may avoid repeating expensive factory/construction work after a template exists.

---

## 23. Builder + Prototype Together

These patterns can cooperate.

First create a complex template using Builder:

```java
User template = new User.Builder()
        .name("Default User")
        .email("default@example.com")
        .age(25)
        .build();
```

Then copy it repeatedly:

```java
User user1 = template.copy();
User user2 = template.copy();
User user3 = template.copy();
```

Conceptually:

```text
Builder
  |
  | creates/configures once
  v
Prototype template
  |
  +---- copy() ---> Object #1
  +---- copy() ---> Object #2
  +---- copy() ---> Object #3
```

---

## 24. Advantages

Prototype can provide:

- reuse of expensive configuration,
- fewer repeated construction steps,
- runtime-defined templates,
- encapsulated copy logic,
- convenient creation of similar objects,
- reduced coupling to concrete constructors,
- natural support for prototype registries.

---

## 25. Disadvantages

Prototype adds complexity when:

- object graphs contain mutable nested state,
- ownership rules are unclear,
- cyclic graphs exist,
- resources cannot meaningfully be copied,
- references intentionally need sharing,
- identity-sensitive fields must change,
- copying can duplicate sensitive or invalid runtime state.

Deep-copy semantics can be significantly harder than the top-level `copy()` method suggests.

---

## 26. When to Use Prototype

Consider Prototype when:

```text
Object creation is expensive
OR
Object configuration is complicated
OR
Many similar objects are needed
OR
Runtime templates exist
OR
Construction details should remain hidden
```

Typical examples:

- configured document templates,
- game entities,
- UI component templates,
- workflow/configuration templates,
- simulation objects,
- test fixtures,
- domain objects with reusable baseline configurations.

---

## 27. When NOT to Use Prototype

Avoid Prototype when:

- the object is cheap and trivial to construct,
- copy semantics are ambiguous,
- the object holds non-copyable resources,
- copying can violate identity/invariant rules,
- sharing immutable objects is simpler,
- a normal constructor/factory communicates intent more clearly.

For example:

```java
new Point(10, 20);
```

is usually clearer than introducing a prototype infrastructure merely to duplicate a two-field value object.

---

## 28. Common Prototype Pitfalls

### Pitfall 1 — Mistaking reference assignment for copying

```java
User user2 = user1; // same object
```

### Pitfall 2 — Assuming `super.clone()` performs deep copy

It does not. It is shallow by default.

### Pitfall 3 — Copying the collection but not mutable elements

```java
new ArrayList<>(roles)
```

copies the list structure, not mutable `Role` objects.

### Pitfall 4 — Blind deep copy

Not every object should be duplicated. Immutable/shared resources may intentionally remain shared.

### Pitfall 5 — Copying identity fields incorrectly

If a copied entity should represent a new database identity, copying an existing primary key can be wrong.

### Pitfall 6 — Copying live resources

Sockets, threads, database connections, file handles, locks, executors, and transactions do not usually have meaningful clone semantics.

### Pitfall 7 — Forgetting cycles

Recursive deep copy without tracking visited objects can recurse forever.

---

## 29. Database Entity Warning

Be careful when applying Prototype to persistence entities.

Suppose:

```java
class UserEntity {
    Long id;
    String email;
}
```

A copy like:

```java
this.id = other.id;
```

may incorrectly represent the same database identity.

Often the desired semantics for a new entity are:

```java
this.id = null;
```

while copying business configuration.

So Prototype must preserve **semantic identity rules**, not merely Java fields.

---

## 30. Thread Safety

Copying does not automatically make an object thread-safe.

A shallow copy may still share mutable objects:

```text
copy A ----+
           +----> shared mutable List
copy B ----+
```

Concurrent mutation can still race.

A deep copy can provide independent mutable state, but thread safety still depends on how each resulting object is used.

Immutable prototypes are especially convenient because shared immutable state can safely remain shared.

---

## 31. Interview Questions and Answers

### Q1. What is Prototype Pattern?

A creational pattern that creates new objects by copying existing configured objects rather than reconstructing them from scratch.

### Q2. Is assigning one reference to another Prototype?

No. That creates another reference to the same object, not a new object.

### Q3. What is shallow copy?

A new top-level object is created, but nested object references are copied and therefore shared.

### Q4. What is deep copy?

A new top-level object is created and mutable nested state that requires independent ownership is also copied.

### Q5. Does Java `Object.clone()` perform deep copy?

No. `super.clone()` performs a shallow field-by-field copy.

### Q6. Is `Cloneable` required for Prototype Pattern?

No. Prototype can be implemented using `copy()`, copy constructors, factories, or other explicit mechanisms.

### Q7. Why are copy constructors often preferred?

They are explicit, type-safe, can enforce invariants, and make ownership decisions visible without relying on Java's awkward `Cloneable` contract.

### Q8. Do immutable nested objects need deep copies?

Usually no. Because their state cannot change, safely sharing them normally preserves independence from mutation.

### Q9. Is `new ArrayList<>(oldList)` a complete deep copy?

Only if the elements are immutable or intentionally shared. Mutable elements remain shared.

### Q10. How do you deep-copy a cyclic graph?

Track already-copied source objects in an identity map and reuse the corresponding copy when a node is encountered again.

### Q11. What is a Prototype Registry?

A collection of configured prototype instances indexed by a key; clients obtain new objects by looking up and copying a prototype.

### Q12. Prototype vs Factory?

Factory usually chooses and constructs a type; Prototype creates from an existing configured instance.

### Q13. Prototype vs Builder?

Builder constructs step by step; Prototype duplicates an existing configured object.

### Q14. What is the hardest part of Prototype?

Defining correct copy semantics: what must be independently copied, what may remain shared, and what must be regenerated or excluded.

### Q15. Can Prototype copy resources such as DB connections?

Usually it should not. Resource handles have lifecycle and ownership semantics that do not map cleanly to cloning.

---

## 32. Fast Revision Diagram

```text
                  PROTOTYPE PATTERN

              Existing configured object
                        |
                        | copy()/clone()
                        v
                    New object


Reference assignment:

A ------+
        +----> Same Object
B ------+


Shallow copy:

A ----> Object #1 ----+
                      +----> Shared Mutable Child
B ----> Object #2 ----+


Deep copy:

A ----> Object #1 ----> Child #1

B ----> Object #2 ----> Child #2
```

---

## 33. Fast Revision Table

| Topic | Remember |
|---|---|
| Intent | Create new object by copying existing one |
| Category | Creational |
| Reference assignment | Not a copy |
| Shallow copy | New parent, shared nested references |
| Deep copy | Independent mutable nested state where needed |
| Immutable fields | Usually safe to share |
| `super.clone()` | Shallow field copy |
| `Cloneable` | Java mechanism, not required by pattern |
| Copy constructor | Explicit modern Java-friendly technique |
| Registry | Stores configured prototypes for lookup/copy |
| Builder difference | Builder configures step by step |
| Factory difference | Factory chooses/constructs; Prototype copies |
| Main risk | Incorrect ownership/copy semantics |
| Collections | Copying container alone may still share elements |
| Cycles | Track already-copied objects |

---

## 34. One-Minute Interview Answer

> Prototype is a creational design pattern used to create a new object by copying an existing configured object. It is useful when construction is expensive or when many objects share a common baseline configuration. The main design concern is copy semantics. A shallow copy creates a new top-level object but shares nested references; a deep copy creates independent copies of mutable nested state that requires separate ownership. In Java, Prototype can be implemented with a copy constructor or an explicit `copy()` method; `Cloneable` is not required, and `super.clone()` is shallow by default. A Prototype Registry can hold preconfigured templates and return copies by key.

---

# Java Interview Coding Example — Deep Copy

```java
import java.util.ArrayList;
import java.util.List;

public final class User {

    private final String name;
    private final Address address;
    private final List<Role> roles;

    public User(String name, Address address, List<Role> roles) {
        this.name = name;
        this.address = address;
        this.roles = new ArrayList<>(roles);
    }

    private User(User other) {
        this.name = other.name;
        this.address = new Address(other.address);
        this.roles = other.roles.stream()
                .map(Role::new)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public User copy() {
        return new User(this);
    }
}

final class Address {
    private String city;
    private String country;

    Address(String city, String country) {
        this.city = city;
        this.country = country;
    }

    Address(Address other) {
        this.city = other.city;
        this.country = other.country;
    }
}

final class Role {
    private String name;

    Role(String name) {
        this.name = name;
    }

    Role(Role other) {
        this.name = other.name;
    }
}
```

The important point is not merely the syntax. It is that the copying policy explicitly states which mutable objects are independently owned.

---

# Go Awareness — Prototype

Go does not have Java-style `Cloneable`. Prototype is normally implemented explicitly.

```go
type User struct {
    Name    string
    Address Address
}

type Address struct {
    City string
}

func (u User) Copy() User {
    return User{
        Name: u.Name,
        Address: Address{
            City: u.Address.City,
        },
    }
}
```

For slices, maps, pointers, and nested mutable structures, explicitly decide whether to share or duplicate backing data.

---

# Final Mental Model

```text
Creational Patterns Learned
===========================

Factory
→ Which object/type should I create?

Builder
→ How should I configure and construct it?

Prototype
→ Which existing object should I copy?
```

For Prototype, the interview-critical concept is:

```text
Copying syntax is easy.
Correct ownership semantics are the real design problem.
```
