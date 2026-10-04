# Composite Pattern — Revision Notes

## 1. Core Idea

Use the **Composite Pattern** when you have individual objects and groups of those objects, and you want clients to treat both uniformly.

> Composite organizes objects into tree structures and lets clients treat individual objects and compositions of objects through the same interface.

Mental model:

```text
                    Component
                   <<interface>>
                    /       \
                   /         \
                Leaf       Composite
                             |
                             | contains
                             v
                       List<Component>
```

Core relationships:

```text
Leaf IS-A Component

Composite IS-A Component

Composite HAS-A List<Component>
```

---

## 2. What Problem Does Composite Solve?

Classic example:

```text
File
Directory
```

A `File` is an individual object.

A `Directory` may contain:

```text
Files
+
other Directories
```

Example:

```text
root/
├── resume.pdf
├── photo.jpg
└── documents/
    ├── notes.txt
    └── projects/
        └── design.pdf
```

This is naturally a tree.

Without Composite, client code may need repeated type checks and separate traversal rules.

---

## 3. Problem Without Composite

You might initially model:

```java
public class Directory {

    private List<File> files;
    private List<Directory> directories;
}
```

Then every operation becomes duplicated:

```java
for (File file : files) {
    ...
}

for (Directory directory : directories) {
    ...
}
```

Client code may start using:

```java
if (item instanceof File) {
    ...
} else if (item instanceof Directory) {
    ...
}
```

Composite removes this type-based branching.

---

## 4. Component Interface

```java
public interface FileSystemComponent {

    String getName();

    long getSize();
}
```

Both Leaf and Composite implement the same abstraction.

---

## 5. Leaf

A `File` cannot contain children.

```java
public class File
        implements FileSystemComponent {

    private final String name;
    private final long size;

    public File(
            String name,
            long size
    ) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }
}
```

Relationship:

```text
File IS-A FileSystemComponent
```

---

## 6. Composite

```java
import java.util.ArrayList;
import java.util.List;

public class Directory
        implements FileSystemComponent {

    private final String name;

    private final List<FileSystemComponent>
            children =
            new ArrayList<FileSystemComponent>();

    public Directory(String name) {
        this.name = name;
    }

    public void add(
            FileSystemComponent component
    ) {
        children.add(component);
    }

    public void remove(
            FileSystemComponent component
    ) {
        children.remove(component);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {

        long totalSize = 0;

        for (FileSystemComponent child
                : children) {

            totalSize +=
                    child.getSize();
        }

        return totalSize;
    }
}
```

Relationships:

```text
Directory IS-A FileSystemComponent

Directory HAS-A List<FileSystemComponent>
```

---

## 7. Why List<FileSystemComponent> Matters

We use:

```java
List<FileSystemComponent>
```

instead of:

```java
List<File>
```

or:

```java
List<Directory>
```

because both `File` and `Directory` are `FileSystemComponent`.

This enables recursive nesting:

```text
Directory
    |
    +-- File
    |
    +-- Directory
           |
           +-- File
           |
           +-- Directory
```

---

## 8. Client Code

```java
public class Client {

    public static void main(String[] args) {

        FileSystemComponent resume =
                new File(
                        "resume.pdf",
                        100
                );

        FileSystemComponent photo =
                new File(
                        "photo.jpg",
                        200
                );

        FileSystemComponent notes =
                new File(
                        "notes.txt",
                        50
                );

        Directory documents =
                new Directory(
                        "documents"
                );

        documents.add(notes);

        Directory root =
                new Directory(
                        "root"
                );

        root.add(resume);
        root.add(photo);
        root.add(documents);

        System.out.println(
                root.getSize()
        );
    }
}
```

Output:

```text
350
```

---

## 9. Recursive Delegation

The important code is:

```java
for (FileSystemComponent child
        : children) {

    totalSize += child.getSize();
}
```

No `instanceof` checks are needed.

If child is a `File`:

```text
File.getSize()
```

runs.

If child is a `Directory`:

```text
Directory.getSize()
```

runs recursively.

Polymorphism handles the tree.

---

## 10. Uniform Treatment

The client can work with:

```java
FileSystemComponent component;
```

Then call:

```java
component.getSize();
```

without caring whether the runtime object is:

```text
File
or
Directory
```

This is the defining property of Composite.

---

## 11. Why Is It Called Composite?

A Composite:

```text
IS-A Component
```

and is composed of:

```text
other Components
```

Example:

```text
Directory
    IS-A FileSystemComponent

AND

Directory
    HAS-A List<FileSystemComponent>
```

That recursive structure creates a tree.

---

## 12. Adding Another Operation

Add:

```java
void print();
```

Component:

```java
public interface FileSystemComponent {

    String getName();

    long getSize();

    void print();
}
```

Leaf:

```java
@Override
public void print() {

    System.out.println(
            "File: " + name
    );
}
```

Composite:

```java
@Override
public void print() {

    System.out.println(
            "Directory: " + name
    );

    for (FileSystemComponent child
            : children) {

        child.print();
    }
}
```

Now one call recursively prints the whole tree.

---

## 13. Common Use Cases

```text
File systems
Organization hierarchies
UI trees
Menus
Product bundles
Categories
Arithmetic expression trees
HTML/XML DOM-like structures
```

Composite is especially useful when the domain forms a recursive part-whole hierarchy.

---

## 14. Organization Hierarchy Example

```text
CEO
├── Engineering Manager
│   ├── Engineer
│   └── Engineer
└── Sales Manager
    ├── Salesperson
    └── Salesperson
```

Possible abstraction:

```text
EmployeeComponent
        ^
        |
   +----+----+
   |         |
Employee   Manager
             |
             v
     List<EmployeeComponent>
```

Then both individual employee and team can answer a common operation like total cost.

---

## 15. Product Bundle Example

```text
Gaming Bundle
├── Laptop
├── Mouse
└── Accessories Bundle
    ├── Keyboard
    └── Headset
```

Component:

```java
public interface ProductComponent {

    double getPrice();
}
```

Leaf:

```text
Product
```

Composite:

```text
Bundle
```

Then:

```java
bundle.getPrice();
```

recursively calculates total price.

---

## 16. Safe vs Transparent Composite

Where should child-management methods live?

### Transparent Composite

Put them on `Component`:

```java
public interface FileSystemComponent {

    long getSize();

    void add(
            FileSystemComponent component
    );

    void remove(
            FileSystemComponent component
    );
}
```

Benefits:

```text
Maximum uniformity
```

Downside:

```text
Leaf exposes meaningless add/remove operations
```

A Leaf may need to throw:

```java
UnsupportedOperationException
```

---

## 17. Safe Composite

Keep child-management only on Composite:

```java
public class Directory
        implements FileSystemComponent {

    public void add(...) {
    }

    public void remove(...) {
    }
}
```

Benefits:

```text
Meaningful type-safe API
Leaves expose only valid operations
```

Downside:

If the client only has a `Component` reference, it cannot manage children without knowing the object is a Composite.

---

## 18. Safe vs Transparent Summary

```text
Transparent
-----------
add/remove on Component

+ maximum uniformity
- Leaf gets operations that make no sense


Safe
----
add/remove only on Composite

+ cleaner meaningful API
- less uniform for child management
```

For ordinary Java domain code, the safe approach is often preferable.

---

## 19. Composite vs Decorator

Both use `HAS-A Component`, but intent differs.

Decorator:

```text
Decorator
   |
   | usually wraps ONE
   v
Component
```

Composite:

```text
Composite
   |
   | contains MANY
   v
List<Component>
```

Memory:

```text
Decorator = wrapper chain

Composite = tree
```

Intent:

```text
Decorator
→ add behavior

Composite
→ model part-whole hierarchy
```

---

## 20. Composite vs Facade

Facade:

```text
Facade
  |
  +--> Service A
  +--> Service B
  +--> Service C
```

Composite:

```text
Component
  |
  +--> Component
  +--> Component
  +--> Component
```

Difference:

```text
Facade children may be unrelated subsystem classes

Composite children share the same Component abstraction
```

---

## 21. Composite vs Iterator

They complement each other.

```text
Composite
→ represents the tree structure

Iterator
→ represents traversal through the tree
```

A Composite can expose custom tree iterators.

---

## 22. Composite vs Chain of Responsibility

CoR:

```text
Request
  |
  v
Handler
  |
  v
Handler
```

Composite:

```text
Component
   /   \
  /     \
Component Component
```

Summary:

```text
CoR = request forwarding

Composite = recursive part-whole structure
```

---

## 23. Composite Operations Are Not Only Aggregations

Common operations include:

```text
render()
print()
move()
delete()
calculatePrice()
calculateSize()
validate()
execute()
```

The key is that the operation makes sense for both Leaf and Composite.

---

## 24. UI Tree Example

```text
Window
├── Button
├── TextField
└── Panel
    ├── Button
    └── Label
```

Component:

```java
public interface UIComponent {

    void render();
}
```

Leaves:

```text
Button
Label
TextField
```

Composites:

```text
Panel
Window
```

Then:

```java
window.render();
```

recursively renders children.

---

## 25. Arithmetic Expression Example

Expression:

```text
        +
       / \
      5   *
         / \
        2   3
```

Component:

```java
public interface Expression {

    int evaluate();
}
```

Leaf:

```java
public class NumberExpression
        implements Expression {

    private final int value;

    public NumberExpression(int value) {
        this.value = value;
    }

    @Override
    public int evaluate() {
        return value;
    }
}
```

Composite:

```java
public class AddExpression
        implements Expression {

    private final Expression left;
    private final Expression right;

    public AddExpression(
            Expression left,
            Expression right
    ) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int evaluate() {

        return left.evaluate()
                + right.evaluate();
    }
}
```

Same structural idea:

```text
Leaf IS-A Component
Composite IS-A Component
Composite HAS-A Component(s)
```

---

## 26. Cycle Risk

Composite is usually intended to form a tree.

Bad:

```text
Directory A
   |
   v
Directory B
   |
   v
Directory A
```

Calling a recursive operation can loop forever until:

```text
StackOverflowError
```

Real implementations may need to prevent cycles when adding children.

---

## 27. Ownership Concerns

Suppose:

```java
directoryA.add(file);
directoryB.add(file);
```

Can the same child belong to two parents?

That depends on the domain.

Composite Pattern does not define ownership semantics.

Your domain must decide.

---

## 28. Advantages

```text
Uniform treatment of leaves and groups
Natural tree representation
Recursive operations become simple
Client avoids type checks
Easy to add new Component types
Part-whole hierarchy is explicit
```

---

## 29. Disadvantages

```text
Common interface can become too broad
Some operations may not fit every Leaf
Recursive structures can be harder to debug
Cycle risks
Large trees may have performance concerns
```

---

## 30. When to Use

Good signals:

```text
Domain naturally forms a tree

Groups can contain individuals or other groups

Same operation should work on both Leaf and Composite

Client should avoid instanceof/type branching
```

Typical examples:

```text
File systems
Menus
UI containers
Organization charts
Product bundles
Expression trees
```

---

## 31. When Not to Use

Avoid when:

```text
There is no recursive part-whole hierarchy
Leaf and group behavior differ fundamentally
A common Component abstraction would be artificial
```

---

## 32. Full Java 8 Implementation

### Component

```java
public interface FileSystemComponent {

    String getName();

    long getSize();

    void print();
}
```

### Leaf

```java
public class File
        implements FileSystemComponent {

    private final String name;
    private final long size;

    public File(
            String name,
            long size
    ) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void print() {

        System.out.println(
                "File: "
                + name
                + " ("
                + size
                + ")"
        );
    }
}
```

### Composite

```java
import java.util.ArrayList;
import java.util.List;

public class Directory
        implements FileSystemComponent {

    private final String name;

    private final List<FileSystemComponent>
            children =
            new ArrayList<FileSystemComponent>();

    public Directory(String name) {
        this.name = name;
    }

    public void add(
            FileSystemComponent component
    ) {
        children.add(component);
    }

    public void remove(
            FileSystemComponent component
    ) {
        children.remove(component);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {

        long totalSize = 0;

        for (FileSystemComponent child
                : children) {

            totalSize +=
                    child.getSize();
        }

        return totalSize;
    }

    @Override
    public void print() {

        System.out.println(
                "Directory: " + name
        );

        for (FileSystemComponent child
                : children) {

            child.print();
        }
    }
}
```

### Client

```java
public class Client {

    public static void main(String[] args) {

        FileSystemComponent resume =
                new File(
                        "resume.pdf",
                        100
                );

        FileSystemComponent photo =
                new File(
                        "photo.jpg",
                        200
                );

        FileSystemComponent notes =
                new File(
                        "notes.txt",
                        50
                );

        Directory documents =
                new Directory(
                        "documents"
                );

        documents.add(notes);

        Directory root =
                new Directory(
                        "root"
                );

        root.add(resume);
        root.add(photo);
        root.add(documents);

        root.print();

        System.out.println(
                "Total size: "
                + root.getSize()
        );
    }
}
```

---

## 33. Object Structure in Memory

```text
root : Directory
 |
 | children
 |
 +----> resume : File
 |
 +----> photo : File
 |
 +----> documents : Directory
             |
             | children
             |
             +----> notes : File
```

All children are referenced through:

```java
FileSystemComponent
```

---

## 34. Architecture to Remember

```text
                    <<interface>>
                      Component
                          ^
                          |
              +-----------+-----------+
              |                       |
            Leaf                  Composite
                                      |
                                      | HAS-A many
                                      v
                              List<Component>
```

Memorize:

```text
Leaf IS-A Component

Composite IS-A Component

Composite HAS-A List<Component>
```

---

## 35. Common Interview Traps

### Trap 1
"Composite is just inheritance."

No. Recursive composition is essential.

### Trap 2
"Composite always means summing values."

No. Any common recursive operation may apply.

### Trap 3
"Leaf and Composite must expose add/remove."

Not necessarily. Safe and Transparent variants exist.

### Trap 4
"Composite structures can never have cycles."

They can accidentally, unless the domain prevents them.

---

## 36. Interview Mental Model

If you hear:

> "Bundles can contain products or other bundles."

Think:

```text
Composite
```

If:

> "Directories contain files and directories."

Think:

```text
Composite
```

If:

> "Containers contain controls and other containers."

Think:

```text
Composite
```

Core sentence:

> Composite organizes objects into tree structures and lets clients treat individual objects and compositions uniformly through a common interface.

---

## 37. Fast Revision Table

| Concept | Meaning |
|---|---|
| Component | Common abstraction |
| Leaf | Individual object |
| Composite | Group/container |
| Children | `List<Component>` |
| Main structure | Recursive tree |
| Safe Composite | Child management only on Composite |
| Transparent Composite | Child management on Component |
| Main benefit | Uniform Leaf/Composite treatment |

---

## 38. One-Line Structural Memory Aid

```text
Adapter = compatibility

Decorator = behavior enhancement

Facade = simplification

Proxy = access control

Composite = part-whole tree / uniform treatment
```
