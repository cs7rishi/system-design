# Command Pattern — Revision Notes

## 1. Core Idea

**Command Pattern** encapsulates a request/action as an object.

Instead of directly calling:

```java
light.turnOn();
```

we wrap the action:

```java
Command command = new LightOnCommand(light);
command.execute();
```

Mental model:

```text
Client
  |
  v
Command
  |
  v
Receiver
```

The key benefit is that the caller does not need to know the concrete receiver or its method details.

---

## 2. What Problem Does Command Solve?

Without Command:

```java
public class RemoteControl {

    private Light light;
    private Fan fan;

    public void pressLightButton() {
        light.turnOn();
    }

    public void pressFanButton() {
        fan.start();
    }
}
```

Problems:

```text
RemoteControl
   |
   +-- knows Light
   +-- knows Fan
   +-- knows exact methods
```

As more devices/actions are added, the caller becomes tightly coupled.

Command replaces direct method calls with a common abstraction:

```java
command.execute();
```

Now the Invoker only knows `Command`.

---

## 3. Core Architecture

```text
Client
  |
  | creates/wires
  v
Command ---------> Receiver
  ^
  |
  | HAS-A
Invoker
```

Roles:

```text
Client
→ creates and wires objects

Invoker
→ knows WHEN to execute

Command
→ represents WHAT request/action to execute

Receiver
→ knows HOW to perform the actual operation
```

---

## 4. Basic Java 8 Implementation

### Command interface

```java
public interface Command {
    void execute();
}
```

### Receiver

```java
public class Light {

    public void turnOn() {
        System.out.println("Light ON");
    }

    public void turnOff() {
        System.out.println("Light OFF");
    }
}
```

### Concrete Command

```java
public class LightOnCommand implements Command {

    private final Light light;

    public LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }
}
```

### Invoker

```java
public class RemoteControl {

    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }
}
```

### Client

```java
public class Client {

    public static void main(String[] args) {

        Light light = new Light();

        Command command =
                new LightOnCommand(light);

        RemoteControl remote =
                new RemoteControl();

        remote.setCommand(command);
        remote.pressButton();
    }
}
```

Output:

```text
Light ON
```

---

## 5. Important Relationships

```text
RemoteControl HAS-A Command

LightOnCommand IS-A Command

LightOnCommand HAS-A Light
```

Diagram:

```text
                 <<interface>>
                    Command
                 +----------+
                 | execute()|
                 +----+-----+
                      ^
                      |
              implements
                      |
               LightOnCommand
                      |
                      | HAS-A
                      v
                    Light

RemoteControl
     |
     | HAS-A
     v
  Command
```

---

## 6. Why Requests as Objects Matter

Before:

```java
light.turnOn();
```

This is just a method call.

With Command:

```java
Command command =
        new LightOnCommand(light);
```

Now the request itself is an object.

That means it can be:

```text
stored
queued
logged
scheduled
retried
executed later
grouped
undone
```

This is one of the deepest benefits of Command Pattern.

---

## 7. Programmable Invoker

The same Invoker can execute different commands:

```java
remote.setCommand(new LightOnCommand(light));
remote.pressButton();

remote.setCommand(new FanStartCommand(fan));
remote.pressButton();
```

The Invoker does not change.

```text
RemoteControl
     |
     +--> LightOnCommand
     |
     +--> FanStartCommand
     |
     +--> GarageOpenCommand
```

---

## 8. Undo Support

Extend the interface:

```java
public interface Command {

    void execute();

    void undo();
}
```

Concrete Command:

```java
public class LightOnCommand implements Command {

    private final Light light;

    public LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }

    @Override
    public void undo() {
        light.turnOff();
    }
}
```

Invoker:

```java
public class RemoteControl {

    private Command command;
    private Command lastCommand;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
        lastCommand = command;
    }

    public void pressUndo() {
        if (lastCommand != null) {
            lastCommand.undo();
        }
    }
}
```

Flow:

```text
execute()
   |
   v
Light ON

undo()
   |
   v
Light OFF
```

---

## 9. Undo With Previous State

Undo is not always simply the opposite method.

Example: thermostat.

```java
public class SetTemperatureCommand implements Command {

    private final Thermostat thermostat;
    private final int newTemperature;
    private int previousTemperature;

    public SetTemperatureCommand(
            Thermostat thermostat,
            int newTemperature
    ) {
        this.thermostat = thermostat;
        this.newTemperature = newTemperature;
    }

    @Override
    public void execute() {
        previousTemperature =
                thermostat.getTemperature();

        thermostat.setTemperature(
                newTemperature
        );
    }

    @Override
    public void undo() {
        thermostat.setTemperature(
                previousTemperature
        );
    }
}
```

Key idea:

```text
Command may store state
needed to reverse the request.
```

---

## 10. Macro Command

A command can contain multiple commands.

```java
import java.util.List;

public class MacroCommand implements Command {

    private final List<Command> commands;

    public MacroCommand(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void execute() {

        for (Command command : commands) {
            command.execute();
        }
    }
}
```

Example:

```text
GoodNightCommand
   |
   +-- LightOffCommand
   +-- TVOffCommand
   +-- LockDoorCommand
   +-- ACOnCommand
```

This lets one command represent a group of actions.

---

## 11. Queueing / Delayed Execution

Because a command is an object:

```java
Queue<Command> queue =
        new LinkedList<Command>();

queue.add(new SendEmailCommand(emailService));
queue.add(new GenerateReportCommand(reportService));
```

Later:

```java
while (!queue.isEmpty()) {

    Command command = queue.remove();

    command.execute();
}
```

Architecture:

```text
Producer
   |
   v
Command
   |
   v
Queue
   |
   v
Worker
   |
   v
Receiver
```

This enables delayed execution.

---

## 12. Command vs Strategy

Both use composition, but intent differs.

### Strategy

```text
Question:
HOW should this behavior be performed?
```

Example:

```text
Routing
   |
   +-- Walking
   +-- Driving
   +-- Cycling
```

### Command

```text
Question:
WHAT request/action should be executed?
```

Example:

```text
Command
   |
   +-- Turn Light On
   +-- Open Garage
   +-- Send Email
```

Summary:

```text
Strategy
→ interchangeable algorithm

Command
→ encapsulated request/action
```

---

## 13. Command vs Observer

### Command

```text
Invoker
   |
   v
Command
   |
   v
Receiver
```

Usually focused on triggering a request.

### Observer

```text
Subject
   |
   +-- Observer A
   +-- Observer B
   +-- Observer C
```

Focused on notifying multiple listeners about an event/state change.

---

## 14. Command vs Factory

Factory:

```text
Which object should I create?
```

Command:

```text
Which action/request should I execute?
```

Factory may create Commands, but they solve different problems.

---

## 15. Common Use Cases

```text
GUI button actions
Undo/redo systems
Remote controls
Job queues
Task schedulers
Transaction requests
Menu actions
Command-line tooling
Workflow actions
Macro operations
```

---

## 16. Advantages

```text
Loose coupling between sender and receiver
Requests become first-class objects
Easy undo/redo support
Queueing and delayed execution
Command logging
Macro/grouped commands
Easy to add new command types
```

---

## 17. Disadvantages

```text
More classes
More indirection
Can be overkill for simple direct calls
Undo can require extra state
Command explosion in large systems
```

---

## 18. Common Interview Traps

### Trap 1 — "Command is just a wrapper"

Too shallow.

The deeper value is:

```text
request becomes an object
```

which enables queueing, logging, scheduling, undo, and composition.

### Trap 2 — "Undo is always opposite method"

Not always.

Sometimes you must store prior state.

### Trap 3 — "Invoker performs business logic"

No.

Invoker triggers the command.

Receiver performs the actual domain operation.

### Trap 4 — "Command and Strategy are same"

No.

```text
Strategy
→ behavior/algorithm choice

Command
→ request/action representation
```

---

## 19. Interview Mental Model

If you hear:

```text
queue actions
delay execution
undo/redo
log operations
schedule jobs
decouple button from action
macro command
```

think:

```text
Command Pattern
```

Core sentence:

> Command Pattern encapsulates a request as an object, decoupling the sender from the receiver and enabling operations such as queueing, logging, scheduling, grouping, and undo/redo.

---

## 20. Fast Revision Table

| Concept | Meaning |
|---|---|
| Command | Request represented as object |
| Concrete Command | Implements request |
| Receiver | Performs actual work |
| Invoker | Triggers command |
| Client | Wires command, receiver, invoker |
| `execute()` | Performs request |
| `undo()` | Reverses request when possible |
| Macro Command | Groups multiple commands |
| Queueing | Commands can be stored and executed later |

---

## 21. One-Line Memory Aid

```text
Strategy = HOW

Command = WHAT ACTION

Observer = WHO SHOULD BE NOTIFIED
```
