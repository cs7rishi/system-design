# Creational Design Patterns — Map

```text
Factory
→ WHICH object/implementation should I create?

Builder
→ HOW should I construct a complex object?

Prototype
→ WHICH existing object should I copy?
```

## Factory
```text
Factory = select object creation
```
Centralizes/selects concrete object creation so consuming code can depend on abstractions.

## Builder
```text
Builder = step-by-step construction
```
Useful for many optional/configurable fields, readable construction, validation, and immutable final products.

Java mental model:
```text
Product
  +-- private constructor
  +-- final fields
  +-- static nested Builder
```

## Prototype
```text
Prototype = copy an existing configured object
```
Key interview topic:
```text
Shallow copy vs Deep copy
```
Deep-copy mutable state that must be independently owned; immutable state can generally be shared.

| Pattern | Mental Question |
|---|---|
| Factory | WHICH object should I create? |
| Builder | HOW should I construct it? |
| Prototype | WHICH existing object should I copy? |
