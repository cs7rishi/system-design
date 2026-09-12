# Low Level Design & Design Patterns Reference Notes

This document provides companion notes for each concept in `curriculum.txt`. For every pattern, it specifies the problem it solves, when to use it, resemblances to other patterns, how to make design decisions, and concrete before/after code implementations.

---

# Section 1: Principles & Fundamentals

## 1. What is LLD and Pattern Categories?
* **Low Level Design (LLD)**: The phase of software architecture where abstract high-level components are translated into detailed class diagrams, interfaces, method signatures, data structures, and relational mechanics.
* **Pattern Categories**:
  1. **Creational**: Deal with object creation mechanisms to decouple clients from direct class instantiations.
  2. **Structural**: Deal with object composition and class structure to form larger, flexible systems without tight coupling.
  3. **Behavioral**: Deal with algorithms and assignment of responsibilities between collaborating objects.
  4. **Architectural**: High-level patterns (like MVC) that govern the interaction between application-tier components.

---

## 2. Difference Between Is-a & Has-a Relationship
* **Is-a (Inheritance)**: Represents class subtyping (e.g., `MallardDuck is-a Duck`). It creates a compile-time static relationship where the subclass inherits all behaviors of the parent, which can lead to rigid designs and inappropriate method inheritance.
* **Has-a (Composition)**: Represents containment/delegation (e.g., `Duck has-a FlyBehavior`). The container holds a reference to an interface and delegates work dynamically at runtime, allowing interchangeable algorithms and greater flexibility.
* **Core Rule**: Favor Composition (HAS-A) over Inheritance (IS-A).

---

## 3. SOLID Principles

### S - Single Responsibility Principle (SRP)
* A class should have only one reason to change, meaning it should encapsulate a single job or responsibility.

### O - Open/Closed Principle (OCP)
* Software artifacts should be open for extension, but closed for modification. You should be able to add new functionality without touching existing, tested source code.

### L - Liskov Substitution Principle (LSP)
* Subtypes must be substitutable for their base types without altering the correctness of the program. Derived classes should honor the contracts established by their parent interfaces without throwing unexpected runtime exceptions or neutralizing behavior.

### I - Interface Segregation Principle (ISP)
* Clients should not be forced to depend upon interfaces they do not use. Split fat, monolithic interfaces into smaller, role-specific interfaces.

### D - Dependency Inversion Principle (DIP)
* High-level modules should not depend on low-level modules; both should depend on abstractions. Abstractions should not depend on details; details should depend on abstractions.

---

## 4. Liskov Substitution Principle (LSP) Solution
* **Problem**: Subclasses throw exceptions or override methods to do nothing because they inherit methods that do not apply to them (e.g., a `Square` subclassing `Rectangle`, or a `RubberDuck` inheriting `fly()`).
* **Solution**: Extract the differing behaviors into specialized sub-interfaces or separate components instead of forcing them down the inheritance chain.

---

## 5. DRY (Don't Repeat Yourself) & KISS (Keep It Simple, Stupid)
* **DRY**: Every piece of knowledge or business logic must have a single, unambiguous, authoritative representation within a system. Avoid duplicate algorithms across classes.
* **KISS**: Software systems work best when kept simple rather than made complicated. Avoid premature optimization and unnecessary design layers until requirements genuinely demand them.

---

# Section 2: Design Patterns

---

## Behavioral Patterns

### 1. Strategy Design Pattern
* **What problems it solves**: Solves the problem of rigid inheritance and code duplication when subclasses require differing variations of behavior (e.g., ducks that fly differently or cannot fly at all).
* **When it should be used**: When you have a family of algorithms/behaviors, want to encapsulate each one, and make them interchangeable at runtime.
* **Resembles with**: State Pattern.
* **How to decide usage vs resembling pattern**: Use Strategy when the client configures/injects the desired algorithm. Use State when the object transitions its behavior automatically based on internal state changes.
* **Before Code (Problem)**:
```java
public abstract class Duck {
    public void fly() { System.out.println("Flying"); }
}
public class RubberDuck extends Duck {
    // Overriding to do nothing breaks flexibility and LSP
    public void fly() { /* Do nothing */ }
}
```
* **After Code (Strategy Pattern)**:
```java
public interface FlyBehavior {
    void fly();
}
public class FlyWithWings implements FlyBehavior {
    public void fly() { System.out.println("Flying with wings"); }
}
public class FlyNoWay implements FlyBehavior {
    public void fly() { System.out.println("Cannot fly"); }
}

public class Duck {
    protected FlyBehavior flyBehavior;
    public void performFly() { flyBehavior.fly(); }
    public void setFlyBehavior(FlyBehavior fb) { this.flyBehavior = fb; }
}
```

---

### 2. Observer Pattern
* **What problems it solves**: Polling for updates and tight coupling between state-holding objects and listening consumers.
* **When it should be used**: When a change to one object requires updating a dynamic list of dependent objects automatically (one-to-many dependency).
* **Resembles with**: Publish-Subscribe, Mediator.
* **How to decide usage vs resembling pattern**: Use Observer when subjects and observers have direct contract interfaces. Use Pub-Sub when producers and consumers are decoupled via a dedicated event bus. Use Mediator to manage arbitrary many-to-many communication.
* **Before Code (Problem)**:
```java
public class WeatherData {
    public void measurementsChanged() {
        float temp = getTemperature();
        // Hardcoded direct coupling
        currentConditionsDisplay.update(temp);
        statisticsDisplay.update(temp);
    }
}
```
* **After Code (Observer Pattern)**:
```java
public interface Observer {
    void update(float temp, float humidity, float pressure);
}
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
public class WeatherData implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private float temp, humidity, pressure;

    public void registerObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o) { observers.remove(o); }
    public void notifyObservers() {
        for (Observer o : observers) { o.update(temp, humidity, pressure); }
    }
}
```

---

### 3. Chain Of Responsibility Design Pattern
* **What problems it solves**: Tight coupling between the sender of a request and its receivers, and complex conditional chains checking request handling logic.
* **When it should be used**: When more than one object can handle a request, the handler is determined dynamically, or requests must pass through sequential filters (e.g., Auth -> Logger -> Validator).
* **Resembles with**: Command, Interceptor.
* **How to decide usage vs resembling pattern**: Use Command to encapsulate a single invocation. Use Chain of Responsibility to pass a request along an ordered pipeline of handlers.
* **Before Code (Problem)**:
```java
public class RequestHandler {
    public void handle(Request req) {
        if (req.isAuth()) {
            if (req.isValid()) {
                // Execute logic
            }
        }
    }
}
```
* **After Code (Chain of Responsibility Pattern)**:
```java
public abstract class Handler {
    protected Handler next;
    public void setNext(Handler next) { this.next = next; }
    public abstract void handle(Request req);
}

public class AuthHandler extends Handler {
    public void handle(Request req) {
        if (req.isAuth()) {
            if (next != null) next.handle(req);
        } else {
            System.out.println("Authentication failed");
        }
    }
}

public class ValidationHandler extends Handler {
    public void handle(Request req) {
        if (req.isValid()) {
            if (next != null) next.handle(req);
        }
    }
}
```

---

### 4. Null Object Design Pattern
* **What problems it solves**: Pervasive null checks (`if (obj != null)`) that clutter code and cause `NullPointerException` bugs.
* **When it should be used**: When a default "do nothing" or neutral behavior can stand in place of a null object reference.
* **Resembles with**: Strategy, State.
* **How to decide usage vs resembling pattern**: Null Object provides an inert implementation of an interface solely to eliminate defensive null checks.
* **Before Code (Problem)**:
```java
Customer c = database.findCustomer(id);
if (c != null) {
    c.sendNotification();
}
```
* **After Code (Null Object Pattern)**:
```java
public interface Customer {
    void sendNotification();
    boolean isNull();
}

public class RealCustomer implements Customer {
    public void sendNotification() { System.out.println("Sending notification"); }
    public boolean isNull() { return false; }
}

public class NullCustomer implements Customer {
    public void sendNotification() { /* Do nothing safely */ }
    public boolean isNull() { return true; }
}
```

---

### 5. State Design Pattern
* **What problems it solves**: Huge conditional statements (`switch` or `if-else`) governing an object's behavior depending on internal state transitions.
* **When it should be used**: When an object must change its behavior at runtime as its internal state changes (e.g., Vending Machine, Order lifecycle).
* **Resembles with**: Strategy.
* **How to decide usage vs resembling pattern**: Strategy lets the client choose the behavior at creation. State allows the context object to transition its behaviors dynamically across its lifecycle.
* **Before Code (Problem)**:
```java
public class VendingMachine {
    int state = 0; // 0: NO_COIN, 1: HAS_COIN
    public void insertCoin() {
        if (state == 0) { state = 1; }
        else { System.out.println("Coin already inserted"); }
    }
}
```
* **After Code (State Pattern)**:
```java
public interface State {
    void insertCoin();
    void ejectCoin();
}

public class NoCoinState implements State {
    private VendingMachine machine;
    public NoCoinState(VendingMachine m) { this.machine = m; }
    public void insertCoin() {
        System.out.println("Coin inserted");
        machine.setState(machine.getHasCoinState());
    }
    public void ejectCoin() { System.out.println("No coin to eject"); }
}
```

---

### 6. Iterator Design Pattern
* **What problems it solves**: Exposing the internal structure of collections (Lists, Arrays, HashMaps) to clients during traversal.
* **When it should be used**: To access elements of an aggregate collection sequentially without exposing its underlying storage data structure.
* **Resembles with**: Composite.
* **How to decide usage vs resembling pattern**: Composite organizes elements in hierarchies; Iterator provides linear traversal across those structures.
* **Before Code (Problem)**:
```java
// Client must know underlying data structure specifics
for (int i = 0; i < arrayItems.length; i++) { ... }
for (int i = 0; i < listItems.size(); i++) { ... }
```
* **After Code (Iterator Pattern)**:
```java
public interface Iterator<T> {
    boolean hasNext();
    T next();
}

public class ArrayIterator implements Iterator<String> {
    private String[] items;
    private int pos = 0;
    public ArrayIterator(String[] items) { this.items = items; }
    public boolean hasNext() { return pos < items.length && items[pos] != null; }
    public String next() { return items[pos++]; }
}
```

---

### 7. Mediator Design Pattern
* **What problems it solves**: Tight, unmaintainable many-to-many coupling between communicating peer objects.
* **When it should be used**: When complex communication webs exist between UI components or system services, and direct references make the classes unmanageable.
* **Resembles with**: Observer, Facade.
* **How to decide usage vs resembling pattern**: Observer provides a one-way notification broadcast. Facade simplifies an underlying system one-way. Mediator coordinates bidirectional conversations between peer components.
* **Before Code (Problem)**:
```java
public class Checkbox {
    private Button submitBtn;
    public void check() {
        // Direct dependency between widgets
        submitBtn.setEnabled(true);
    }
}
```
* **After Code (Mediator Pattern)**:
```java
public interface Mediator {
    void notify(Component sender, String event);
}

public class DialogMediator implements Mediator {
    private Button submitBtn;
    private Checkbox checkbox;

    public void notify(Component sender, String event) {
        if (sender == checkbox && event.equals("checked")) {
            submitBtn.setEnabled(true);
        }
    }
}
```

---

### 8. Visitor Design Pattern
* **What problems it solves**: Modifying stable class hierarchies whenever a new operation or algorithm needs to run over them.
* **When it should be used**: When you need to define new operations on complex object structures (such as ASTs or document elements) without altering the elements themselves.
* **Resembles with**: Iterator, Composite.
* **How to decide usage vs resembling pattern**: Iterator simply traverses elements. Visitor applies double dispatch (`accept(v)` -> `visit(this)`) to execute externalized logic across differing types.
* **Before Code (Problem)**:
```java
public abstract class DocumentElement {
    // New operations force modifying all existing element classes
    public abstract void exportToPdf();
    public abstract void exportToJson();
}
```
* **After Code (Visitor Pattern)**:
```java
public interface Visitor {
    void visit(TextElement te);
    void visit(ImageElement ie);
}

public interface DocumentElement {
    void accept(Visitor v);
}

public class TextElement implements DocumentElement {
    public void accept(Visitor v) { v.visit(this); }
}

public class PdfExportVisitor implements Visitor {
    public void visit(TextElement te) { System.out.println("Exporting Text to PDF"); }
    public void visit(ImageElement ie) { System.out.println("Exporting Image to PDF"); }
}
```

---

### 9. Memento Design Pattern
* **What problems it solves**: Capturing and restoring an object's internal state without exposing its private attributes or breaking encapsulation.
* **When it should be used**: When building undo/redo mechanisms, transaction rollbacks, or checkpoint snapshots.
* **Resembles with**: Command, Prototype.
* **How to decide usage vs resembling pattern**: Command encapsulates the execution of an action. Memento captures the state snapshot of an object before or after the action executes.
* **Before Code (Problem)**:
```java
public class TextEditor {
    public String text; // Exposing private fields so other classes can save state
}
```
* **After Code (Memento Pattern)**:
```java
public class EditorMemento {
    private final String state;
    public EditorMemento(String state) { this.state = state; }
    public String getState() { return state; }
}

public class TextEditor {
    private String content;
    public void setContent(String c) { this.content = c; }
    public EditorMemento save() { return new EditorMemento(content); }
    public void restore(EditorMemento m) { this.content = m.getState(); }
}
```

---

### 10. Template Method Design Pattern
* **What problems it solves**: Algorithm structure duplication where multiple subclasses execute the same general steps in sequence, differing only in specific sub-steps.
* **When it should be used**: When subclasses share an invariant execution workflow but need custom step implementations (inverting control: *Don't call us, we'll call you*).
* **Resembles with**: Strategy.
* **How to decide usage vs resembling pattern**: Template Method uses inheritance with a `final` algorithm skeleton in the superclass. Strategy uses composition to swap out entire algorithm strategies at runtime.
* **Before Code (Problem)**:
```java
public class Coffee {
    public void prepare() {
        boilWater(); brewCoffee(); pourInCup(); addSugar();
    }
}
public class Tea {
    public void prepare() {
        boilWater(); steepTea(); pourInCup(); addLemon(); // Duplicated workflow
    }
}
```
* **After Code (Template Method Pattern)**:
```java
public abstract class Beverage {
    public final void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
    }
    protected abstract void brew();
    protected abstract void addCondiments();
    private void boilWater() { System.out.println("Boiling water"); }
    private void pourInCup() { System.out.println("Pouring into cup"); }
}

public class Tea extends Beverage {
    protected void brew() { System.out.println("Steeping tea"); }
    protected void addCondiments() { System.out.println("Adding lemon"); }
}
```

---

### 11. Interpreter Pattern
* **What problems it solves**: Parsing and evaluating expressions formatted according to a recurring domain-specific grammar or syntax rules.
* **When it should be used**: When a language or rule structure can be represented as an Abstract Syntax Tree (AST), such as mathematical equations, boolean filters, or simple SQL queries.
* **Resembles with**: Composite.
* **How to decide usage vs resembling pattern**: Interpreter relies directly on Composite structures to hold grammar trees, adding specific `interpret()` evaluations to grammar nodes.
* **Before Code (Problem)**:
```java
public boolean evaluate(String expr) {
    // Nested conditional string splits and brittle parsing
    if (expr.contains("AND")) { ... }
}
```
* **After Code (Interpreter Pattern)**:
```java
public interface Expression {
    boolean interpret(String context);
}

public class TerminalExpression implements Expression {
    private String data;
    public TerminalExpression(String data) { this.data = data; }
    public boolean interpret(String context) { return context.contains(data); }
}

public class AndExpression implements Expression {
    private Expression expr1, expr2;
    public AndExpression(Expression e1, Expression e2) { this.expr1 = e1; this.expr2 = e2; }
    public boolean interpret(String context) {
        return expr1.interpret(context) && expr2.interpret(context);
    }
}
```

---

### 12. Command Design Pattern
* **What problems it solves**: Direct coupling between UI triggers / callers and the receiver executing the underlying work.
* **When it should be used**: When parameterizing objects with operations, queueing tasks, logging execution histories, or supporting undo/redo.
* **Resembles with**: Strategy, Memento.
* **How to decide usage vs resembling pattern**: Strategy encapsulates algorithms for how a job is done. Command encapsulates the actual request and parameters into a standalone invocable object.
* **Before Code (Problem)**:
```java
public class RemoteButton {
    private Light light; // Hardcoded to a specific hardware device
    public void press() { light.turnOn(); }
}
```
* **After Code (Command Pattern)**:
```java
public interface Command {
    void execute();
    void undo();
}

public class LightOnCommand implements Command {
    private Light light;
    public LightOnCommand(Light l) { this.light = l; }
    public void execute() { light.turnOn(); }
    public void undo() { light.turnOff(); }
}

public class SimpleRemote {
    private Command slot;
    public void setCommand(Command c) { this.slot = c; }
    public void pressButton() { slot.execute(); }
}
```

---

## Structural Patterns

### 13. Decorator Design Pattern
* **What problems it solves**: Class explosions resulting from subclassing every permutation of optional features or behaviors.
* **When it should be used**: When responsibilities should be attached to individual objects dynamically and transparently at runtime without altering existing classes.
* **Resembles with**: Proxy, Composite.
* **How to decide usage vs resembling pattern**: Decorator dynamically augments behavior. Proxy regulates/controls access. Composite organizes objects into part-whole hierarchies.
* **Before Code (Problem)**:
```java
public class CoffeeWithMilkAndSugarAndCaramel extends Beverage {
    public double cost() { return 1.50; } // Combinatorial explosion of classes
}
```
* **After Code (Decorator Pattern)**:
```java
public abstract class Beverage {
    public abstract double cost();
}

public abstract class CondimentDecorator extends Beverage {
    protected Beverage beverage;
}

public class Milk extends CondimentDecorator {
    public Milk(Beverage b) { this.beverage = b; }
    public double cost() { return 0.20 + beverage.cost(); }
}
// Usage: Beverage order = new Milk(new Sugar(new SimpleCoffee()));
```

---

### 14. Proxy Design Pattern
* **What problems it solves**: Managing access to costly objects, executing lazy initialization, controlling permissions, or communicating across boundaries (remote objects).
* **When it should be used**: For lazy loading (Virtual Proxy), access restriction (Protection Proxy), or network calls (Remote Proxy).
* **Resembles with**: Decorator, Adapter.
* **How to decide usage vs resembling pattern**: Proxy preserves the exact interface to control access. Decorator preserves the interface to add responsibilities. Adapter alters an interface to bridge incompatibilities.
* **Before Code (Problem)**:
```java
public class RealImage {
    public RealImage(String path) {
        loadDiskFile(path); // Loads 500MB asset instantly even if never displayed
    }
    public void display() { ... }
}
```
* **After Code (Proxy Pattern)**:
```java
public interface Image {
    void display();
}

public class RealImage implements Image {
    private String path;
    public RealImage(String path) { this.path = path; loadDiskFile(path); }
    public void display() { System.out.println("Displaying " + path); }
    private void loadDiskFile(String p) { /* Expensive IO */ }
}

public class ProxyImage implements Image {
    private RealImage realImage;
    private String path;
    public ProxyImage(String path) { this.path = path; }

    public void display() {
        if (realImage == null) {
            realImage = new RealImage(path); // Loaded on first demand
        }
        realImage.display();
    }
}
```

---

### 15. Composite Pattern
* **What problems it solves**: Writing duplicated or nested code to handle single leaf items versus group nodes differently.
* **When it should be used**: When representing tree structures where clients must treat individual elements and aggregate groups uniformly.
* **Resembles with**: Decorator, Iterator.
* **How to decide usage vs resembling pattern**: Composite organizes objects into hierarchical trees. Decorator wraps objects to attach behavior. Iterator traverses collections.
* **Before Code (Problem)**:
```java
public class Directory {
    List<File> files;
    List<Directory> subDirs;
    public void displayAll() {
        for (File f : files) f.show();
        for (Directory d : subDirs) d.displayAll(); // Manual branching
    }
}
```
* **After Code (Composite Pattern)**:
```java
public interface FileSystemNode {
    void showDetails();
}

public class FileItem implements FileSystemNode {
    private String name;
    public FileItem(String name) { this.name = name; }
    public void showDetails() { System.out.println("File: " + name); }
}

public class Folder implements FileSystemNode {
    private List<FileSystemNode> children = new ArrayList<>();
    public void add(FileSystemNode node) { children.add(node); }
    public void showDetails() {
        for (FileSystemNode node : children) { node.showDetails(); }
    }
}
```

---

### 16. Adapter Pattern
* **What problems it solves**: Incompatible interfaces preventing two classes from collaborating without altering original code.
* **When it should be used**: When integrating legacy components, 3rd party SDKs, or external services that don't match the interface your system expects.
* **Resembles with**: Facade, Proxy.
* **How to decide usage vs resembling pattern**: Adapter converts an existing interface to match a target interface. Facade creates a simpler interface over an entire system. Proxy matches the interface identically to control access.
* **Before Code (Problem)**:
```java
public interface MediaPlayer {
    void play(String file);
}
public class AdvancedPlayer {
    public void playVlc(String file) { /* Incompatible method signature */ }
}
```
* **After Code (Adapter Pattern)**:
```java
public class MediaAdapter implements MediaPlayer {
    private AdvancedPlayer advancedPlayer;
    public MediaAdapter(AdvancedPlayer ap) { this.advancedPlayer = ap; }
    public void play(String file) {
        advancedPlayer.playVlc(file); // Adapting call
    }
}
```

---

### 17. Facade Design Pattern
* **What problems it solves**: Exposing complex subsystems to client code, creating brittle architectures and high coupling.
* **When it should be used**: To provide a simple, unified interface to an entire complex subsystem (e.g., Home Theater, Payment gateway integration).
* **Resembles with**: Adapter, Mediator.
* **How to decide usage vs resembling pattern**: Facade simplifies an entire subsystem one-way. Adapter converts one interface into another. Mediator coordinates communication between peers.
* **Before Code (Problem)**:
```java
// Client must interact directly with many separate subsystem pieces
lights.dim(10);
projector.on();
projector.setInput(dvd);
amplifier.on();
amplifier.setVolume(7);
player.play();
```
* **After Code (Facade Pattern)**:
```java
public class HomeTheaterFacade {
    private Lights lights;
    private Projector projector;
    private Amplifier amp;
    private DvdPlayer player;

    public HomeTheaterFacade(Lights l, Projector p, Amplifier a, DvdPlayer dp) {
        this.lights = l; this.projector = p; this.amp = a; this.player = dp;
    }

    public void watchMovie() {
        lights.dim(10);
        projector.on();
        amp.on();
        player.play();
    }
}
```

---

### 18. Bridge Design Pattern
* **What problems it solves**: Exponential multiplication of classes when abstractions and their concrete implementations both vary independently.
* **When it should be used**: When an abstraction (e.g., RemoteControl) and its implementation (e.g., Device) should be decoupled so both can be extended independently via inheritance.
* **Resembles with**: Adapter, Strategy.
* **How to decide usage vs resembling pattern**: Bridge is designed upfront to decouple an abstraction from its implementation. Adapter is introduced after development to make incompatible classes work together.
* **Before Code (Problem)**:
```java
// Inflexible class matrix
public class BasicSonyRemote extends SonyRemote { }
public class AdvancedSonyRemote extends SonyRemote { }
public class BasicLgRemote extends LgRemote { }
public class AdvancedLgRemote extends LgRemote { }
```
* **After Code (Bridge Pattern)**:
```java
public interface Device {
    void turnOn();
    void turnOff();
}

public class Tv implements Device {
    public void turnOn() { System.out.println("TV On"); }
    public void turnOff() { System.out.println("TV Off"); }
}

public abstract class RemoteControl {
    protected Device device;
    public RemoteControl(Device d) { this.device = d; }
    public abstract void togglePower();
}

public class BasicRemote extends RemoteControl {
    public BasicRemote(Device d) { super(d); }
    public void togglePower() { device.turnOn(); }
}
```

---

### 19. Flyweight Design Pattern
* **What problems it solves**: High memory usage and application crashes when managing huge numbers of identical or overlapping fine-grained objects.
* **When it should be used**: When an application creates a massive number of similar objects that can share immutable intrinsic state (e.g., fonts, game textures, particle systems).
* **Resembles with**: Singleton, Object Pool.
* **How to decide usage vs resembling pattern**: Singleton limits a class to one global instance. Flyweight manages multiple reusable instances that isolate intrinsic state from extrinsic state.
* **Before Code (Problem)**:
```java
public class Particle {
    int x, y;
    byte[] heavySpriteSheet; // Repeated 100,000 times in memory
}
```
* **After Code (Flyweight Pattern)**:
```java
// Shared intrinsic state
public class Sprite {
    private byte[] spriteSheet;
    public Sprite(String asset) { /* Load expensive sprite data */ }
    public void draw(int x, int y) { /* Render at specific coordinates */ }
}

// Extrinsic instance
public class Particle {
    private int x, y;
    private Sprite sprite; // Shared reference
    public Particle(int x, int y, Sprite s) { this.x = x; this.y = y; this.sprite = s; }
}
```

---

## Creational Patterns

### 20. Factory & Abstract Factory Pattern
* **What problems it solves**: Direct instantiation of concrete classes via `new`, which creates rigid dependencies and violates the Open/Closed Principle.
* **When it should be used**:
  * **Factory Method**: When a class delegates object creation to subclasses via inheritance.
  * **Abstract Factory**: When a client needs to create families of related/dependent products without specifying their concrete classes.
* **Resembles with**: Builder, Prototype.
* **How to decide usage vs resembling pattern**: Use Factory Method for single products. Use Abstract Factory for families of products. Use Builder to construct complex multi-part objects step-by-step.
* **Before Code (Problem)**:
```java
public Pizza orderPizza(String type) {
    Pizza p;
    if (type.equals("ny")) p = new NYStyleCheesePizza();
    else p = new ChicagoStyleCheesePizza();
    p.prepare();
    return p;
}
```
* **After Code (Factory Method & Abstract Factory Pattern)**:
```java
// Abstract Factory Interface for ingredients
public interface PizzaIngredientFactory {
    Dough createDough();
}

// Factory Method in Creator Class
public abstract class PizzaStore {
    public Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type);
        pizza.prepare();
        return pizza;
    }
    protected abstract Pizza createPizza(String type);
}

public class NYPizzaStore extends PizzaStore {
    protected Pizza createPizza(String type) {
        PizzaIngredientFactory factory = new NYIngredientFactory();
        return new NYCheesePizza(factory);
    }
}
```

---

### 21. Builder Design Pattern
* **What problems it solves**: Massive telescoping constructors, unclear parameter ordering, and objects left in partially initialized states.
* **When it should be used**: When constructing a complex object requires many configuration steps or involves multiple optional fields.
* **Resembles with**: Abstract Factory.
* **How to decide usage vs resembling pattern**: Abstract Factory creates simple or family objects in a single pass. Builder constructs a complex object step-by-step and returns the product when invoked.
* **Before Code (Problem)**:
```java
User u = new User("John", "Doe", null, 25, null, true, "1234567890"); // Telescoping constructor anti-pattern
```
* **After Code (Builder Pattern)**:
```java
public class User {
    private final String firstName;
    private final String lastName;
    private final int age;

    private User(UserBuilder b) {
        this.firstName = b.firstName;
        this.lastName = b.lastName;
        this.age = b.age;
    }

    public static class UserBuilder {
        private String firstName;
        private String lastName;
        private int age;

        public UserBuilder(String first, String last) {
            this.firstName = first;
            this.lastName = last;
        }
        public UserBuilder age(int age) { this.age = age; return this; }
        public User build() { return new User(this); }
    }
}
```

---

### 22. Object Pool Design Pattern
* **What problems it solves**: Performance bottlenecks and garbage collection overhead caused by repeatedly allocating and deallocating expensive system resources.
* **When it should be used**: When instances are computationally expensive to create (e.g., database connections, network sockets), and the application requires a pool of reusable instances.
* **Resembles with**: Singleton, Factory Method.
* **How to decide usage vs resembling pattern**: Singleton restricts instantiation to one instance. Object Pool provides a collection of reusable instances checked out and returned by clients.
* **Before Code (Problem)**:
```java
public void executeQuery() {
    Connection conn = new Connection(); // Very high setup cost per query
    conn.execute("SELECT 1");
    conn.close();
}
```
* **After Code (Object Pool Pattern)**:
```java
public class ConnectionPool {
    private List<Connection> freeConnections = new ArrayList<>();

    public synchronized Connection acquireConnection() {
        if (freeConnections.isEmpty()) {
            return new Connection();
        }
        return freeConnections.remove(0);
    }

    public synchronized void releaseConnection(Connection c) {
        freeConnections.add(c);
    }
}
```

---

## Architectural Patterns

### 23. MVC (Model-View-Controller)
* **What problems it solves**: Spaghetti architecture caused by combining business logic, data models, and user interface rendering in a single layer.
* **When it should be used**: In applications with user interfaces where presentation, state management, and user interaction logic must be decoupled and tested independently.
* **Resembles with**: MVP (Model-View-Presenter), MVVM (Model-View-ViewModel).
* **How to decide usage vs resembling pattern**: Use MVC when the View observes the Model directly. In MVP, the Presenter mediates all updates between View and Model. In MVVM, two-way data binding connects the View to the ViewModel.
* **Code Implementation (Compound Pattern Structure)**:
```java
// Model (Holds state, extends Subject / Observable)
public class BeatModel {
    private int bpm = 60;
    private List<Observer> observers = new ArrayList<>();
    public void setBPM(int bpm) {
        this.bpm = bpm;
        notifyObservers();
    }
    public int getBPM() { return bpm; }
    public void registerObserver(Observer o) { observers.add(o); }
    private void notifyObservers() { for (Observer o : observers) o.update(); }
}

// Controller (Encapsulates user commands and controls the Model)
public class BeatController {
    private BeatModel model;
    public BeatController(BeatModel model) { this.model = model; }
    public void increaseBPM() { model.setBPM(model.getBPM() + 1); }
    public void decreaseBPM() { model.setBPM(model.getBPM() - 1); }
}

// View (Renders the Model and passes user actions to Controller)
public class BeatView implements Observer {
    private BeatModel model;
    private BeatController controller;

    public BeatView(BeatModel m, BeatController c) {
        this.model = m;
        this.controller = c;
        this.model.registerObserver(this);
    }
    public void onUserClickPlus() { controller.increaseBPM(); }
    public void update() { System.out.println("Displaying BPM: " + model.getBPM()); }
}
```

---

# Section 3: LLD Real-World System Design Problems

To design each system, apply the principles and patterns detailed above:

1. **Design Parking Lot**:
   * *Patterns applied*: Factory (for vehicle/ticket creation), Strategy (hourly vs daily fee calculation), Observer (updating electronic display boards on spot occupancy).
2. **Design Tic-Tac-Toe Game**:
   * *Patterns applied*: State (current player turn, game over states), Strategy (pluggable AI player vs human player).
3. **LLD of Elevator System**:
   * *Patterns applied*: State (MovingUp, MovingDown, Idle), Strategy (elevator dispatch algorithms like SCAN, LOOK).
4. **LLD of Car Rental System**:
   * *Patterns applied*: Factory (car types), Decorator (add-ons like GPS, child seats, roadside insurance), State (reserved, rented, returned, maintenance).
5. **LLD of Snake n Ladder Game**:
   * *Patterns applied*: Template Method (game turn execution workflow), Composite/State (board squares containing snakes, ladders, or normal positions).
6. **LLD of BookMyShow | Design Movie Ticket Booking App**:
   * *Patterns applied*: Observer (seat booking notifications), Strategy (pricing based on morning/evening shows), Singleton (centralized seat lock manager).
7. **LLD of ATM**:
   * *Patterns applied*: State (Idle, CardInserted, PinEntered, DispensingCash), Chain of Responsibility (dispensing currency denominations: \$100 -> \$50 -> \$20).
8. **LLD of Splitwise**:
   * *Patterns applied*: Strategy (split strategies: EqualSplit, PercentageSplit, ExactSplit), Observer (balance updates to involved participants).
9. **LLD of Cricbuzz**:
   * *Patterns applied*: Observer (pushing real-time commentary and ball-by-ball score updates), Strategy (different scoring/overs algorithms for T20, ODI, Test).
10. **LLD of Inventory Management System**:
    * *Patterns applied*: Observer (low-stock alerts), Strategy (fulfillment and warehouse route dispatching), Singleton (inventory catalog locks).
11. **LLD of Apply Coupons on Shopping Cart Products**:
    * *Patterns applied*: Decorator (chaining multiple discount coupons or percentage discounts on a product), Strategy (coupon validation logic).
12. **LLD of Payment Gateway**:
    * *Patterns applied*: Adapter (unifying different banking and card network APIs like Visa, Mastercard, UPI), Strategy (retry and routing mechanisms), Factory (instantiating payment providers).
