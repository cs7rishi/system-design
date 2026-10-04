# Bridge Pattern — Revision Notes

## 1. Core Idea

Use the **Bridge Pattern** when you have **two independent dimensions of variation** and inheritance would create a class explosion.

> Bridge separates an abstraction from its implementation so both can vary independently.

```text
Abstraction
    |
    | HAS-A
    v
Implementor
```

## 2. Problem It Solves

Without Bridge, two dimensions such as:

```text
Shape: Circle, Square
Color: Red, Blue
```

can create combinations:

```text
RedCircle
BlueCircle
RedSquare
BlueSquare
```

With more variants, subclasses grow roughly as:

```text
m × n
```

Bridge separates those dimensions into two hierarchies connected by composition.

## 3. Shape + Color Example

```java
public interface Color {
    String applyColor();
}
```

```java
public class RedColor implements Color {
    @Override
    public String applyColor() {
        return "Red";
    }
}
```

```java
public class BlueColor implements Color {
    @Override
    public String applyColor() {
        return "Blue";
    }
}
```

```java
public abstract class Shape {

    protected final Color color;

    protected Shape(Color color) {
        this.color = color;
    }

    public abstract void draw();
}
```

```java
public class Circle extends Shape {

    public Circle(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.println(
                "Drawing Circle in "
                + color.applyColor()
        );
    }
}
```

```java
public class Square extends Shape {

    public Square(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.println(
                "Drawing Square in "
                + color.applyColor()
        );
    }
}
```

Client:

```java
Shape redCircle =
        new Circle(
                new RedColor()
        );

Shape blueCircle =
        new Circle(
                new BlueColor()
        );

Shape redSquare =
        new Square(
                new RedColor()
        );
```

## 4. Why Is It Called Bridge?

This reference:

```java
protected final Color color;
```

acts as the bridge between:

```text
Shape hierarchy
     |
     v
Color hierarchy
```

## 5. GoF Terminology

```text
Abstraction
RefinedAbstraction
Implementor
ConcreteImplementor
```

Example mapping:

```text
Shape = Abstraction
Circle/Square = RefinedAbstraction
Color = Implementor
RedColor/BlueColor = ConcreteImplementor
```

## 6. Core Relationships

```text
Circle IS-A Shape
Square IS-A Shape

RedColor IS-A Color
BlueColor IS-A Color

Shape HAS-A Color
```

That last relationship is the Bridge.

## 7. Better Real-World Example — Remote + Device

Two dimensions:

```text
Remote types:
BasicRemote
AdvancedRemote

Devices:
TV
Radio
Projector
```

Without Bridge:

```text
BasicTVRemote
AdvancedTVRemote
BasicRadioRemote
AdvancedRadioRemote
...
```

With Bridge:

```text
Remote hierarchy
      |
      | HAS-A
      v
Device hierarchy
```

### Device

```java
public interface Device {

    void turnOn();

    void turnOff();

    void setVolume(int volume);
}
```

```java
public class TV implements Device {

    @Override
    public void turnOn() {
        System.out.println("TV ON");
    }

    @Override
    public void turnOff() {
        System.out.println("TV OFF");
    }

    @Override
    public void setVolume(int volume) {
        System.out.println(
                "TV volume: " + volume
        );
    }
}
```

```java
public class Radio implements Device {

    @Override
    public void turnOn() {
        System.out.println("Radio ON");
    }

    @Override
    public void turnOff() {
        System.out.println("Radio OFF");
    }

    @Override
    public void setVolume(int volume) {
        System.out.println(
                "Radio volume: " + volume
        );
    }
}
```

### Abstraction

```java
public abstract class RemoteControl {

    protected final Device device;

    protected RemoteControl(
            Device device
    ) {
        this.device = device;
    }

    public void powerOn() {
        device.turnOn();
    }

    public void powerOff() {
        device.turnOff();
    }

    public abstract void volumeUp();
}
```

```java
public class BasicRemote
        extends RemoteControl {

    public BasicRemote(Device device) {
        super(device);
    }

    @Override
    public void volumeUp() {
        device.setVolume(20);
    }
}
```

```java
public class AdvancedRemote
        extends RemoteControl {

    public AdvancedRemote(Device device) {
        super(device);
    }

    @Override
    public void volumeUp() {
        device.setVolume(50);
    }

    public void mute() {
        device.setVolume(0);
    }
}
```

Client:

```java
RemoteControl basicTv =
        new BasicRemote(
                new TV()
        );

RemoteControl advancedTv =
        new AdvancedRemote(
                new TV()
        );

RemoteControl basicRadio =
        new BasicRemote(
                new Radio()
        );
```

## 8. Independent Evolution

Add a new Device:

```text
Projector
```

No Remote changes required.

Add a new Remote:

```text
VoiceRemote
```

No Device changes required.

That is the defining benefit.

## 9. Bridge vs Strategy

```text
Strategy
→ interchangeable algorithm

Bridge
→ separate two independently evolving dimensions
```

Both use composition, but Bridge is broader and structural.

## 10. Bridge vs Adapter

```text
Adapter
→ retrofit compatibility

Bridge
→ design-time separation
```

Adapter handles an existing mismatch.

Bridge prevents tight coupling between dimensions by design.

## 11. Bridge vs Decorator

```text
Decorator
→ stack/add behavior dynamically

Bridge
→ split independent dimensions
```

## 12. Bridge vs Composite

```text
Composite
→ part-whole recursive tree

Bridge
→ two independent hierarchies
```

## 13. Bridge Still Uses Inheritance

Bridge does not eliminate inheritance.

It keeps inheritance inside each independent hierarchy and uses composition between them.

```text
RemoteControl
   ^
   |
BasicRemote
AdvancedRemote
```

and:

```text
Device
   ^
   |
TV
Radio
```

## 14. Mathematical Recognition

Without Bridge:

```text
A variants × B variants
```

can require every combination.

Bridge changes that roughly to:

```text
A variants + B variants
```

This reduces class explosion significantly.

## 15. Another Example — Notification + Provider

```text
Notification Type:
Alert
Marketing
Transactional

Delivery:
Email
SMS
Push
```

Bridge structure:

```text
Notification
    |
    | HAS-A
    v
NotificationSender
```

Each hierarchy can grow independently.

## 16. Advantages

```text
Avoids subclass explosion
Separates independent dimensions
Supports Open/Closed Principle
Reduces coupling
Allows runtime combinations
Each hierarchy evolves independently
```

## 17. Disadvantages

```text
More abstractions/classes
More indirection
Can be overkill
Requires identifying the dimensions correctly
```

## 18. When to Use

Good signals:

```text
Two dimensions both vary
Every A needs versions for every B
Inheritance combinations are multiplying
Both sides need independent extension
```

Examples:

```text
Shape × Color
Remote × Device
Notification Type × Provider
UI Control × Platform Renderer
Report Type × Output Format
Message Type × Transport
```

## 19. When Not to Use

Avoid when:

```text
Only one dimension varies
One side is simple fixed data
No class explosion exists
Composition adds needless complexity
```

## 20. Architecture to Remember

```text
             Abstraction
                 |
                 | HAS-A
                 v
             Implementor
                 ^
                 |
        +--------+--------+
        |                 |
ConcreteImplA      ConcreteImplB


Abstraction
   ^
   |
RefinedA
RefinedB
```

Example:

```text
RemoteControl
    |
    | HAS-A
    v
Device
  /   \
 TV   Radio

RemoteControl
   ^
   |
BasicRemote
AdvancedRemote
```

## 21. Interview Terminology

| Role | Example |
|---|---|
| Abstraction | `RemoteControl` |
| RefinedAbstraction | `BasicRemote`, `AdvancedRemote` |
| Implementor | `Device` |
| ConcreteImplementor | `TV`, `Radio` |
| Bridge | `RemoteControl`'s `Device` reference |

## 22. Common Interview Traps

### Trap 1
"Bridge is just composition."

No. Composition is the mechanism; independent hierarchy separation is the intent.

### Trap 2
"Bridge and Adapter are the same."

No.

```text
Adapter = retrofit compatibility
Bridge = independent dimension separation
```

### Trap 3
"Bridge means no inheritance."

No. Inheritance can remain inside each hierarchy.

### Trap 4
"Any HAS-A relation means Bridge."

No. Both sides should genuinely represent independently varying dimensions.

## 23. Interview Mental Model

If you hear:

> "Every shape requires a version for every color."

Think:

```text
Bridge
```

If you hear:

> "Every remote type requires a TV, Radio, Projector version."

Think:

```text
Bridge
```

Core sentence:

> Bridge separates two independently varying dimensions into separate hierarchies and connects them through composition, avoiding combinatorial inheritance explosion.

## 24. Fast Revision Table

| Concept | Meaning |
|---|---|
| Abstraction | High-level hierarchy |
| RefinedAbstraction | Specialized abstraction |
| Implementor | Separate implementation contract |
| ConcreteImplementor | Implementation variant |
| Bridge | Composition reference |
| Main problem | A × B subclass explosion |
| Main benefit | Independent evolution |

## 25. One-Line Structural Memory Aid

```text
Adapter = compatibility
Decorator = behavior enhancement
Facade = simplification
Proxy = access control
Composite = part-whole tree
Bridge = independent dimensions
```
