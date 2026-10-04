# Template Method Pattern — Revision Notes

## 1. Core Idea

Use **Template Method** when multiple workflows follow the same overall sequence of steps, but some individual steps differ.

The parent class defines:

```text
the algorithm skeleton
the execution order
the common steps
```

Subclasses customize only the varying steps.

Mental model:

```text
              AbstractClass
                   |
          templateMethod()
                   |
        +----------+----------+
        |          |          |
      step1()    step2()    step3()
        |          |          |
      fixed    overridable   fixed
                   ^
                   |
              Subclasses
```

Core sentence:

> Template Method defines the skeleton of an algorithm in a base class while allowing subclasses to override selected steps without changing the overall algorithm structure.

---

## 2. What Problem Does It Solve?

Suppose CSV and JSON processing both follow:

```text
open file
read data
process data
save data
close file
```

Without Template Method:

```java
public class CsvDataProcessor {

    public void process() {

        openFile();

        readCsvData();

        processCsvData();

        saveData();

        closeFile();
    }
}
```

```java
public class JsonDataProcessor {

    public void process() {

        openFile();

        readJsonData();

        processJsonData();

        saveData();

        closeFile();
    }
}
```

Problems:

```text
Duplicate workflow structure
Duplicate common steps
Subclasses/classes may execute steps in wrong order
Changing the workflow requires many changes
```

Template Method centralizes the invariant workflow.

---

## 3. Core Architecture

```text
                    DataProcessor
                   <<abstract>>
                +----------------+
                | final process()|
                +-------+--------+
                        |
           algorithm skeleton
                        |
       +----------------+----------------+
       |                |                |
       v                v                v
   openFile()       readData()      processData()
     fixed           abstract         abstract
                        ^                ^
                        |                |
                +-------+----------------+-------+
                |                              |
         CsvDataProcessor              JsonDataProcessor
```

Relationships:

```text
CsvDataProcessor IS-A DataProcessor

JsonDataProcessor IS-A DataProcessor
```

Key mechanism:

```text
Inheritance + method overriding
```

---

## 4. Basic Java 8 Implementation

```java
public abstract class DataProcessor {

    public final void process() {

        openFile();

        readData();

        processData();

        saveData();

        closeFile();
    }

    private void openFile() {
        System.out.println("Opening file");
    }

    protected abstract void readData();

    protected abstract void processData();

    private void saveData() {
        System.out.println("Saving data");
    }

    private void closeFile() {
        System.out.println("Closing file");
    }
}
```

The method:

```java
public final void process()
```

is the **Template Method**.

It defines the fixed algorithm order.

---

## 5. Why Is the Template Method final?

```java
public final void process()
```

prevents subclasses from replacing the entire workflow.

Without `final`, a subclass could accidentally do:

```java
@Override
public void process() {

    saveData();

    readData();

    // forgot closeFile()
}
```

That would violate the algorithm contract.

Mental model:

```text
Parent controls WHEN and ORDER

Subclass controls HOW selected steps work
```

---

## 6. CSV Implementation

```java
public class CsvDataProcessor
        extends DataProcessor {

    @Override
    protected void readData() {
        System.out.println(
                "Reading CSV data"
        );
    }

    @Override
    protected void processData() {
        System.out.println(
                "Processing CSV data"
        );
    }
}
```

---

## 7. JSON Implementation

```java
public class JsonDataProcessor
        extends DataProcessor {

    @Override
    protected void readData() {
        System.out.println(
                "Reading JSON data"
        );
    }

    @Override
    protected void processData() {
        System.out.println(
                "Processing JSON data"
        );
    }
}
```

---

## 8. Client Code

```java
public class Client {

    public static void main(String[] args) {

        DataProcessor csvProcessor =
                new CsvDataProcessor();

        csvProcessor.process();

        System.out.println();

        DataProcessor jsonProcessor =
                new JsonDataProcessor();

        jsonProcessor.process();
    }
}
```

Output:

```text
Opening file
Reading CSV data
Processing CSV data
Saving data
Closing file

Opening file
Reading JSON data
Processing JSON data
Saving data
Closing file
```

---

## 9. Runtime Dispatch

Suppose:

```java
DataProcessor processor =
        new CsvDataProcessor();

processor.process();
```

Execution flow:

```text
DataProcessor.process()
        |
        v
DataProcessor.openFile()
        |
        v
CsvDataProcessor.readData()
        |
        v
CsvDataProcessor.processData()
        |
        v
DataProcessor.saveData()
        |
        v
DataProcessor.closeFile()
```

The parent owns the workflow.

Java polymorphism dispatches overridable steps to the subclass implementation.

---

## 10. Three Types of Steps

Template Method commonly uses three types of operations.

### Fixed concrete step

```java
private void openFile() {
    ...
}
```

Implemented fully by the parent.

### Required abstract step

```java
protected abstract void readData();
```

Subclass **must** implement it.

### Optional hook

```java
protected void afterProcessing() {
}
```

Subclass **may** override it.

---

## 11. What Is a Hook?

A hook is an optional customization point with a default implementation.

Example:

```java
protected boolean shouldValidate() {
    return false;
}
```

and:

```java
protected void validateData() {
}
```

Template:

```java
public final void process() {

    openFile();

    readData();

    processData();

    if (shouldValidate()) {
        validateData();
    }

    saveData();

    closeFile();
}
```

A subclass can opt in to validation.

---

## 12. Hook Example

```java
public class CsvDataProcessor
        extends DataProcessor {

    @Override
    protected void readData() {
        System.out.println("Reading CSV");
    }

    @Override
    protected void processData() {
        System.out.println("Processing CSV");
    }

    @Override
    protected boolean shouldValidate() {
        return true;
    }

    @Override
    protected void validateData() {
        System.out.println(
                "Validating CSV"
        );
    }
}
```

Flow:

```text
open
 ↓
read
 ↓
process
 ↓
validate
 ↓
save
 ↓
close
```

Another subclass can keep the default behavior and skip validation.

---

## 13. Abstract Method vs Hook

```text
Abstract method
→ required customization

Hook
→ optional customization
```

Example:

```java
protected abstract void readData();
```

must be implemented.

But:

```java
protected boolean shouldValidate() {
    return false;
}
```

does not have to be overridden.

---

## 14. Access Modifier Guidance

Common Java convention:

```text
public
→ template method exposed to clients

protected
→ subclass customization points

private
→ fixed internal steps
```

Example:

```java
public final void process()

protected abstract void readData()

private void openFile()
```

This keeps clients from calling internal workflow steps directly.

---

## 15. Realistic Payment Example

```java
public abstract class PaymentProcessor {

    public final void processPayment(
            double amount
    ) {

        validateAmount(amount);

        authenticate();

        pay(amount);

        sendReceipt(amount);
    }

    private void validateAmount(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid amount"
            );
        }
    }

    protected abstract void authenticate();

    protected abstract void pay(double amount);

    private void sendReceipt(double amount) {
        System.out.println(
                "Receipt sent for " + amount
        );
    }
}
```

Credit Card:

```java
public class CreditCardPaymentProcessor
        extends PaymentProcessor {

    @Override
    protected void authenticate() {
        System.out.println(
                "Validating card details"
        );
    }

    @Override
    protected void pay(double amount) {
        System.out.println(
                "Processing card payment: " + amount
        );
    }
}
```

UPI:

```java
public class UpiPaymentProcessor
        extends PaymentProcessor {

    @Override
    protected void authenticate() {
        System.out.println(
                "Authenticating UPI PIN"
        );
    }

    @Override
    protected void pay(double amount) {
        System.out.println(
                "Processing UPI payment: " + amount
        );
    }
}
```

Invariant flow:

```text
validate
   ↓
authenticate
   ↓
pay
   ↓
send receipt
```

---

## 16. Template Method vs Strategy

This is one of the most important comparisons.

### Template Method

Uses:

```text
INHERITANCE
```

Architecture:

```text
         BaseClass
             ^
             |
       +-----+-----+
       |           |
  Subclass A   Subclass B
```

Intent:

```text
"Customize selected steps of MY algorithm."
```

### Strategy

Uses:

```text
COMPOSITION
```

Architecture:

```text
Context
   |
   | HAS-A
   v
Strategy
   ^
   |
+--+--+
A     B
```

Intent:

```text
"Give me an object that implements the behavior."
```

---

## 17. Runtime Flexibility

Strategy generally provides more runtime flexibility.

```java
processor.setStrategy(
        new CsvStrategy()
);
```

Later:

```java
processor.setStrategy(
        new JsonStrategy()
);
```

With Template Method, behavior is tied to the subclass type:

```java
new CsvDataProcessor();
```

That instance does not dynamically become a JSON processor.

Summary:

```text
Template Method
→ inheritance
→ behavior chosen by subclass/type

Strategy
→ composition
→ behavior can often change at runtime
```

---

## 18. Hollywood Principle

Template Method is associated with:

> "Don't call us, we'll call you."

The parent/framework owns control flow:

```text
Parent.process()
      |
      +--> parent method
      |
      +--> subclass method
      |
      +--> subclass method
      |
      +--> parent method
```

The subclass provides pieces of behavior, but does not control the full execution sequence.

---

## 19. Framework Intuition

Conceptually:

```java
abstract class Framework {

    public final void run() {

        initialize();

        executeUserLogic();

        cleanup();
    }

    protected abstract void executeUserLogic();
}
```

Application:

```java
class MyApplication extends Framework {

    @Override
    protected void executeUserLogic() {
        // custom behavior
    }
}
```

Framework controls:

```text
initialize
   ↓
custom user logic
   ↓
cleanup
```

This is Template Method-style inversion of control.

---

## 20. Why Not Make Everything Abstract?

Bad design:

```java
abstract void open();
abstract void read();
abstract void process();
abstract void save();
abstract void close();
```

Now every subclass repeats common behavior.

Template Method should identify:

```text
what is common
+
what varies
```

Common behavior stays in the base class.

Only varying steps become customization points.

---

## 21. Open/Closed Principle

You can add:

```java
public class XmlDataProcessor
        extends DataProcessor {
}
```

without changing the template workflow.

This works best when:

```text
algorithm skeleton is stable
```

If the overall workflow changes frequently, the base class may become a bottleneck.

---

## 22. Fragile Base Class Problem

Because Template Method relies on inheritance:

```text
Base class changed
       |
       +--> CSV affected
       +--> JSON affected
       +--> XML affected
       +--> Excel affected
```

A change to the base algorithm can impact every subclass.

This is one reason composition is often preferred when flexibility matters more than enforcing one inherited lifecycle.

---

## 23. Template Method vs State

State:

```text
Behavior changes because lifecycle state changes.
```

Template Method:

```text
Algorithm order remains fixed,
but selected steps vary by subclass.
```

Summary:

```text
State
→ lifecycle-dependent behavior

Template Method
→ fixed workflow with customizable steps
```

---

## 24. Template Method vs Chain of Responsibility

Chain of Responsibility:

```text
Request
 ↓
Handler A
 ↓
Handler B
 ↓
Handler C
```

Handlers are separate processing stages.

Template Method:

```text
Base algorithm
 ↓
step1
 ↓
step2
 ↓
step3
```

The parent owns the algorithm sequence.

Summary:

```text
CoR
→ dynamically composed handler pipeline

Template Method
→ statically defined algorithm skeleton
```

---

## 25. Advantages

```text
Removes duplicate workflow structure
Enforces execution order
Reuses common implementation
Makes variation points explicit
Keeps invariant behavior centralized
```

---

## 26. Disadvantages

```text
Inheritance coupling
Fragile base-class risk
Less runtime flexibility than Strategy
Subclass count may grow
Hard to use if workflows differ substantially
```

---

## 27. When to Use

Good signals:

```text
Several classes follow the same workflow

Most steps are shared

A few steps vary

Order must remain consistent

Base class should enforce invariants
```

Typical examples:

```text
File processing
Payment workflow
Report generation
Data import/export
Framework lifecycle
Document processing
Test setup/execution/cleanup
Game loops
```

---

## 28. When Not to Use

Avoid when:

```text
There is no shared algorithm

Behavior must change dynamically at runtime

Subclasses override almost everything

Inheritance is unnatural

Base class would become too large
```

Strategy/composition may be cleaner in those cases.

---

## 29. Common Interview Traps

### Trap 1
"Template Method and Strategy are the same."

No.

```text
Template Method → inheritance
Strategy        → composition
```

### Trap 2
"The template method must be abstract."

No.

The template method is usually concrete and often `final`.

### Trap 3
"Hooks are mandatory."

No.

Hooks are optional extension points.

### Trap 4
"Every step should be overridable."

No.

Fixed invariant steps should stay private/final where appropriate.

---

## 30. Full Java 8 Example

```java
public abstract class DataProcessor {

    public final void process() {

        openFile();

        readData();

        processData();

        if (shouldValidate()) {
            validateData();
        }

        saveData();

        closeFile();
    }

    private void openFile() {
        System.out.println("Opening file");
    }

    protected abstract void readData();

    protected abstract void processData();

    protected boolean shouldValidate() {
        return false;
    }

    protected void validateData() {
        // optional hook
    }

    private void saveData() {
        System.out.println("Saving data");
    }

    private void closeFile() {
        System.out.println("Closing file");
    }
}
```

CSV:

```java
public class CsvDataProcessor
        extends DataProcessor {

    @Override
    protected void readData() {
        System.out.println(
                "Reading CSV data"
        );
    }

    @Override
    protected void processData() {
        System.out.println(
                "Processing CSV data"
        );
    }

    @Override
    protected boolean shouldValidate() {
        return true;
    }

    @Override
    protected void validateData() {
        System.out.println(
                "Validating CSV data"
        );
    }
}
```

JSON:

```java
public class JsonDataProcessor
        extends DataProcessor {

    @Override
    protected void readData() {
        System.out.println(
                "Reading JSON data"
        );
    }

    @Override
    protected void processData() {
        System.out.println(
                "Processing JSON data"
        );
    }
}
```

Client:

```java
public class Client {

    public static void main(String[] args) {

        DataProcessor csv =
                new CsvDataProcessor();

        csv.process();

        System.out.println();

        DataProcessor json =
                new JsonDataProcessor();

        json.process();
    }
}
```

---

## 31. Interview Mental Model

If you hear:

> "All implementations must follow these same steps, in this same order, but a few steps differ."

Think:

```text
Template Method
```

Core sentence:

> Template Method defines the skeleton of an algorithm in a base class while allowing subclasses to override selected steps without changing the overall algorithm structure.

---

## 32. Fast Revision Table

| Concept | Meaning |
|---|---|
| Template Method | Defines algorithm skeleton |
| Concrete step | Shared parent behavior |
| Abstract step | Required subclass customization |
| Hook | Optional subclass customization |
| `final` template | Prevents workflow override |
| Main mechanism | Inheritance |
| Strategy difference | Composition vs inheritance |
| Hollywood Principle | Parent/framework controls execution |

---

## 33. One-Line Memory Aid

```text
Strategy = HOW

Command = WHAT ACTION

Observer = WHO GETS NOTIFIED

State = HOW BEHAVIOR CHANGES WITH STATE

Chain of Responsibility = WHO IN THE CHAIN PROCESSES / PASSES

Template Method = WHICH STEPS OF A FIXED WORKFLOW ARE CUSTOMIZED
```
