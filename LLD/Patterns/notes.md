# Design Patterns Notes

## 1. Strategy Design Pattern (Behavioral)
* **What problems it solves**: Solves the problem of rigid inheritance and code duplication when subclasses need different variations of a behavior (e.g., some ducks fly, some don't, some squeak, some quack). 
* **When it should be used**: When you have a family of algorithms or behaviors, and you want to encapsulate each one and make them interchangeable at runtime.
* **Resembles with**: State Pattern.
* **How to decide usage vs resembling pattern**: Use Strategy when clients choose the algorithm or behavior to plug in (usually tied to the instantiation). Use State when the object changes its behavior automatically based on its internal state lifecycle.
* **Before Code (Problem)**:
```java
public abstract class Duck {
    public void fly() {
        System.out.println("I am flying");
    }
}
public class RubberDuck extends Duck {
    // Problem: Rubber ducks shouldn't fly! We have to override to do nothing.
    public void fly() {
        // Do nothing
    }
}
```
* **After Code (Strategy Pattern)**:
```java
public interface FlyBehavior {
    void fly();
}
public class FlyWithWings implements FlyBehavior {
    public void fly() { System.out.println("Flying with wings!"); }
}
public class FlyNoWay implements FlyBehavior {
    public void fly() { System.out.println("Cannot fly."); }
}

public class Duck {
    FlyBehavior flyBehavior;
    public void performFly() {
        flyBehavior.fly();
    }
    public void setFlyBehavior(FlyBehavior fb) {
        this.flyBehavior = fb;
    }
}
```

## 2. Observer Pattern (Behavioral)
* **What problems it solves**: Polling for state changes or tight coupling where the subject has hardcoded references to the objects that need to be updated.
* **When it should be used**: When you have a one-to-many relationship, and changing the state of one object (Subject) requires notifying all dependent objects (Observers) automatically.
* **Resembles with**: Publish-Subscribe, Mediator.
* **How to decide usage vs resembling pattern**: Observer is generally used when subjects and observers directly know each other's interfaces. Pub-Sub uses an event channel (completely decoupled). Mediator centralizes complex communication between multiple components.
* **Before Code (Problem)**:
```java
public class WeatherData {
    public void measurementsChanged() {
        float temp = getTemperature();
        // Tightly coupled to concrete displays!
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
    public void registerObserver(Observer o) { observers.add(o); }
    public void notifyObservers() {
        for(Observer o : observers) { o.update(temp, humidity, pressure); }
    }
}
```

## 3. Decorator Design Pattern (Structural)
* **What problems it solves**: Class explosion caused by creating subclasses for every possible combination of features (e.g., `HouseBlendWithMochaAndWhip`, `DarkRoastWithSoy`).
* **When it should be used**: To attach additional responsibilities to an object dynamically at runtime. Provides a flexible alternative to subclassing for extending functionality.
* **Resembles with**: Proxy, Composite.
* **How to decide usage vs resembling pattern**: Decorator adds responsibilities/behavior to an object. Proxy controls access to an object. Composite treats a single object and a group of objects uniformly (tree structure).
* **Before Code (Problem)**:
```java
public class DarkRoastWithMochaAndWhip extends Beverage {
    public double cost() {
        return 1.20 + 0.20 + 0.10; // Hardcoded combination costs
    }
}
```
* **After Code (Decorator Pattern)**:
```java
public abstract class Beverage {
    public abstract double cost();
}
public abstract class CondimentDecorator extends Beverage {
    public abstract String getDescription();
}
public class Mocha extends CondimentDecorator {
    Beverage beverage;
    public Mocha(Beverage b) { this.beverage = b; }
    public double cost() {
        return .20 + beverage.cost();
    }
}
// Usage: Beverage b = new Mocha(new Whip(new DarkRoast()));
```

## 4. Factory Method Pattern (Creational)
* **What problems it solves**: Hardcoding object creation using `new` operator which makes the code rigid and difficult to extend if new concrete implementations are added.
* **When it should be used**: When a class cannot anticipate the class of objects it must create, or wants its subclasses to specify the objects it creates.
* **Resembles with**: Abstract Factory, Builder.
* **How to decide usage vs resembling pattern**: Factory Method uses inheritance (subclasses decide which object to instantiate). Abstract Factory uses object composition to create families of related objects. Builder constructs complex objects step-by-step.
* **Before Code (Problem)**:
```java
public Pizza orderPizza(String type) {
    Pizza pizza;
    if (type.equals("cheese")) { pizza = new CheesePizza(); }
    else if (type.equals("pepperoni")) { pizza = new PepperoniPizza(); }
    pizza.prepare();
    return pizza;
}
```
* **After Code (Factory Method)**:
```java
public abstract class PizzaStore {
    public Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type); // Factory method
        pizza.prepare();
        return pizza;
    }
    protected abstract Pizza createPizza(String type);
}
public class NYPizzaStore extends PizzaStore {
    protected Pizza createPizza(String type) {
        if (type.equals("cheese")) return new NYCheesePizza();
        return null;
    }
}
```

## 5. State Design Pattern (Behavioral)
* **What problems it solves**: Massive conditional statements (if-else or switch blocks) used to control an object's behavior based on its current status.
* **When it should be used**: When an object's behavior depends on its state, and it must change its behavior at run-time depending on that state.
* **Resembles with**: Strategy.
* **How to decide usage vs resembling pattern**: State is meant to encapsulate different states of a single context object, allowing it to transition between them internally. Strategy encapsulates different algorithms that are usually injected by a client.
* **Before Code (Problem)**:
```java
public class VendingMachine {
    int state = HAS_COIN;
    public void dispense() {
        if (state == HAS_COIN) { /* dispense */ state = NO_COIN; }
        else if (state == NO_COIN) { /* prompt */ }
    }
}
```
* **After Code (State Pattern)**:
```java
public interface State {
    void dispense();
}
public class HasCoinState implements State {
    VendingMachine machine;
    public HasCoinState(VendingMachine machine) { this.machine = machine; }
    public void dispense() { 
        System.out.println("Dispensing...");
        machine.setState(machine.getNoCoinState());
    }
}
```

## 6. Null Object Design Pattern (Behavioral)
* **What problems it solves**: Repeated null checks (`if (obj != null)`) scattered throughout the code.
* **When it should be used**: When you want to provide a default "do nothing" behavior instead of returning null.
* **Resembles with**: Strategy, State (as a specific state).
* **Before Code (Problem)**:
```java
if (customer != null) {
    customer.makePayment();
}
```
* **After Code (Null Object Pattern)**:
```java
public class NullCustomer implements Customer {
    public void makePayment() {
        // Do nothing implicitly
    }
}
// Usage: customer.makePayment(); // Safe even if it's the NullCustomer
```

*(Note: Similar detailed breakdowns apply for the remaining patterns like Composite, Adapter, Proxy, Builder, Facade, Bridge, Flyweight, Memento, Visitor, Mediator, etc. They follow this exact structure of Problem -> Intent -> Resemblance -> Before Code -> After Code.)*
