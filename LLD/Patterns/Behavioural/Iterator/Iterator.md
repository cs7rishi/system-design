# Iterator Pattern — Revision Notes

## 1. Core Idea

The **Iterator Pattern** lets a client traverse elements of a collection **without exposing how that collection stores those elements internally**.

Mental model:

```text
              Collection
                  |
                  | creates
                  v
               Iterator
                  |
           +------+------+
           |             |
       hasNext()       next()
                         |
                         v
                      Element
```

Core sentence:

> Iterator provides sequential access to elements of an aggregate without exposing its underlying representation, while keeping traversal state and traversal logic inside an iterator object.

---

## 2. What Problem Does Iterator Solve?

Without Iterator, the client may need to know the internal data structure.

Example:

```java
public class Playlist {

    private String[] songs;

    public Playlist(String[] songs) {
        this.songs = songs;
    }

    public String[] getSongs() {
        return songs;
    }
}
```

Client:

```java
String[] songs = playlist.getSongs();

for (int i = 0; i < songs.length; i++) {
    System.out.println(songs[i]);
}
```

Now the client knows:

```text
Playlist uses an array
Uses indexes
Uses .length
Uses songs[i]
```

If the collection changes internally, client traversal code may also need to change.

---

## 3. Desired Design

We want the client to know only:

```text
Do you have another element?

Give me the next element.
```

In code:

```java
iterator.hasNext();

iterator.next();
```

The client should not care whether the data is stored in:

```text
Array
Linked List
Tree
Graph
Custom structure
Database result
```

---

## 4. Custom Iterator Interface

Before using Java's built-in interface:

```java
public interface SongIterator {

    boolean hasNext();

    String next();
}
```

This defines the traversal contract.

---

## 5. Concrete Iterator

```java
public class PlaylistIterator
        implements SongIterator {

    private final String[] songs;

    private int position = 0;

    public PlaylistIterator(String[] songs) {
        this.songs = songs;
    }

    @Override
    public boolean hasNext() {
        return position < songs.length;
    }

    @Override
    public String next() {

        String song = songs[position];

        position++;

        return song;
    }
}
```

Important field:

```java
private int position;
```

The **Iterator owns the traversal state**.

---

## 6. Why Keep Position in the Iterator?

If `Playlist` itself stored:

```java
private int position;
```

then two clients traversing at the same time would interfere with each other.

Bad:

```text
Playlist
   |
   +-- songs
   +-- one shared position
```

Better:

```text
Playlist
   |
   +--------+
   |        |
   v        v
Iterator1  Iterator2
position=1 position=2
```

Each traversal has independent state.

---

## 7. Collection Creates the Iterator

```java
public class Playlist {

    private final String[] songs;

    public Playlist(String[] songs) {
        this.songs = songs;
    }

    public SongIterator createIterator() {

        return new PlaylistIterator(songs);
    }
}
```

The client no longer needs:

```java
getSongs()
```

It asks for a traversal object.

---

## 8. Client Code

```java
public class Client {

    public static void main(String[] args) {

        Playlist playlist =
                new Playlist(
                        new String[]{
                                "Song A",
                                "Song B",
                                "Song C"
                        }
                );

        SongIterator iterator =
                playlist.createIterator();

        while (iterator.hasNext()) {

            String song = iterator.next();

            System.out.println(song);
        }
    }
}
```

Output:

```text
Song A
Song B
Song C
```

The client does not know:

```text
String[]
array indexes
array length
internal storage implementation
```

---

## 9. Core Architecture

Classic Iterator architecture:

```text
                 Collection
                    |
                    | creates
                    v
                 Iterator
               <<interface>>
               +-----------+
               | hasNext() |
               | next()    |
               +-----+-----+
                     ^
                     |
              ConcreteIterator
                     |
                     | traverses
                     v
                Collection
```

GoF terminology:

```text
Iterator
ConcreteIterator
Aggregate
ConcreteAggregate
```

---

## 10. What Actually Changed?

Before:

```text
Client
   |
   | understands traversal details
   v
Collection internals
```

After:

```text
Client
   |
   v
Iterator
   |
   | understands traversal
   v
Collection internals
```

Traversal knowledge moved away from the client.

---

## 11. Internal Representation Can Change

Suppose `Playlist` changes from an array to linked nodes.

Array:

```text
A B C
```

Linked structure:

```text
A -> B -> C -> null
```

A linked-list iterator might use:

```java
private SongNode current;
```

Then:

```java
public boolean hasNext() {
    return current != null;
}
```

```java
public String next() {

    String song = current.getSong();

    current = current.getNext();

    return song;
}
```

The client still uses:

```java
while (iterator.hasNext()) {
    System.out.println(iterator.next());
}
```

---

## 12. Java's Built-In Iterator

In real Java, use:

```java
java.util.Iterator<T>
```

Important methods:

```java
boolean hasNext();

T next();
```

Java 8 implementation:

```java
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PlaylistIterator
        implements Iterator<String> {

    private final String[] songs;

    private int position;

    public PlaylistIterator(String[] songs) {
        this.songs = songs;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < songs.length;
    }

    @Override
    public String next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return songs[position++];
    }
}
```

---

## 13. Iterable vs Iterator

This is an important Java interview distinction.

### Iterable

Represents:

> Something that can provide an iterator.

Conceptually:

```java
public interface Iterable<T> {

    Iterator<T> iterator();
}
```

### Iterator

Represents:

> One active traversal over a collection.

It owns state such as:

```text
current position
current node
stack/queue for traversal
```

Mental model:

```text
Iterable
    |
    | creates
    v
Iterator
    |
    | traverses
    v
Elements
```

Remember:

```text
Iterable = can be iterated

Iterator = performs one iteration
```

---

## 14. Making Playlist Iterable

```java
import java.util.Iterator;

public class Playlist
        implements Iterable<String> {

    private final String[] songs;

    public Playlist(String[] songs) {
        this.songs = songs;
    }

    @Override
    public Iterator<String> iterator() {

        return new PlaylistIterator(songs);
    }
}
```

Now:

```java
Iterator<String> iterator =
        playlist.iterator();
```

works.

---

## 15. Enhanced for Loop

Because `Playlist` implements:

```java
Iterable<String>
```

you can write:

```java
for (String song : playlist) {

    System.out.println(song);
}
```

Conceptually, Java obtains an iterator and traverses through it.

This is why Iterator Pattern is used constantly in normal Java code.

---

## 16. Multiple Independent Iterators

```java
Iterator<String> iterator1 =
        playlist.iterator();

Iterator<String> iterator2 =
        playlist.iterator();
```

Each iterator has independent traversal state.

```text
Playlist
   |
   +--------+
   |        |
   v        v
Iterator1  Iterator2
position=0 position=2
```

This is one major reason traversal state belongs in the Iterator.

---

## 17. Encapsulation Benefit

If `Playlist` exposes its real array:

```java
String[] songs = playlist.getSongs();
```

client code could mutate it:

```java
songs[0] = "Changed";
```

Iterator gives controlled traversal without necessarily exposing the backing data structure.

---

## 18. Multiple Traversal Algorithms

The same collection can support different traversal orders.

Forward:

```text
A -> B -> C -> D
```

Reverse:

```text
D -> C -> B -> A
```

Possible API:

```java
Iterator<String> iterator();

Iterator<String> reverseIterator();
```

Conceptually:

```text
                   Playlist
                  /        \
                 /          \
                v            v
       ForwardIterator   ReverseIterator
            |                 |
        A B C D           D C B A
```

---

## 19. Trees Make Iterator More Valuable

Example tree:

```text
          A
        /   \
       B     C
      / \
     D   E
```

Traversal choices:

```text
Pre-order
A B D E C

In-order
D B E A C

Post-order
D E B C A

Breadth-first
A B C D E
```

Possible design:

```text
Tree
 |
 +-- PreOrderIterator
 |
 +-- InOrderIterator
 |
 +-- BreadthFirstIterator
```

Client still sees:

```java
while (iterator.hasNext()) {
    Node node = iterator.next();
}
```

This is a strong example of separating traversal from storage.

---

## 20. Iterator and Concurrent Modification

Suppose:

```java
List<String> names =
        new ArrayList<String>();

names.add("Alice");
names.add("Bob");
names.add("Charlie");
```

Then modifying the list directly during iteration can result in:

```text
ConcurrentModificationException
```

Example:

```java
for (String name : names) {
    names.remove(name);
}
```

Standard `ArrayList` iterators are typically fail-fast when structural modification is detected outside the iterator.

---

## 21. Iterator remove()

A supported iterator may allow:

```java
iterator.remove();
```

Example:

```java
Iterator<String> iterator =
        names.iterator();

while (iterator.hasNext()) {

    String name = iterator.next();

    if ("Bob".equals(name)) {
        iterator.remove();
    }
}
```

The iterator coordinates removal with its traversal state.

Support depends on the iterator implementation.

---

## 22. Fail-Fast Does Not Mean Thread-Safe

Important distinction:

```text
ConcurrentModificationException
```

is not a concurrency-safety guarantee.

Fail-fast behavior is mainly useful for detecting improper structural modifications during traversal.

It is not a synchronization mechanism.

---

## 23. Iterator vs Strategy

Strategy:

```text
Context
   |
   v
Strategy
```

Encapsulates a general interchangeable algorithm.

Iterator:

```text
Collection
   |
   v
Iterator
```

Encapsulates:

```text
Traversal algorithm
+
Traversal state
```

Summary:

```text
Strategy
→ general behavior algorithm

Iterator
→ traversal algorithm + traversal state
```

---

## 24. Iterator vs Visitor

Useful distinction:

```text
Iterator
→ HOW do I traverse the elements?

Visitor
→ WHAT operation do I perform on different element types?
```

They can work together.

---

## 25. Collection vs Iterator Responsibility

```text
Collection
→ owns/stores elements

Iterator
→ owns traversal state
```

Example:

```text
Playlist
   |
   +-- Song A
   +-- Song B
   +-- Song C

PlaylistIterator
   |
   +-- position
```

---

## 26. Advantages

```text
Traversal without exposing representation

Consistent traversal API

Multiple independent traversals

Multiple traversal algorithms

Traversal state separated from collection

Cleaner client code
```

---

## 27. Disadvantages

```text
Additional objects/classes

Can be unnecessary for trivial structures

Concurrent modification rules can be tricky

Complex structures may require internal stacks/queues
```

Example:

```java
Stack<Node>
```

or:

```java
Queue<Node>
```

may be required for tree traversal.

---

## 28. When to Use

Good signals:

```text
Client needs sequential access

Internal representation should stay hidden

Multiple traversal algorithms exist

Multiple simultaneous traversals are needed

Traversal logic is complex
```

Common examples:

```text
Collections
Trees
Graphs
Playlists
File systems
Database result sets
Custom containers
Paginated data
```

---

## 29. When Not to Use

Avoid unnecessary custom iterators when:

```text
The structure is trivial

Java's built-in collection iteration already solves the need

There is no encapsulation or traversal complexity to hide
```

---

## 30. Full Java 8 Example

### PlaylistIterator

```java
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PlaylistIterator
        implements Iterator<String> {

    private final String[] songs;

    private int position;

    public PlaylistIterator(String[] songs) {
        this.songs = songs;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < songs.length;
    }

    @Override
    public String next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return songs[position++];
    }
}
```

### Playlist

```java
import java.util.Iterator;

public class Playlist
        implements Iterable<String> {

    private final String[] songs;

    public Playlist(String[] songs) {
        this.songs = songs;
    }

    @Override
    public Iterator<String> iterator() {

        return new PlaylistIterator(songs);
    }
}
```

### Client

```java
public class Client {

    public static void main(String[] args) {

        Playlist playlist =
                new Playlist(
                        new String[]{
                                "Song A",
                                "Song B",
                                "Song C"
                        }
                );

        for (String song : playlist) {
            System.out.println(song);
        }
    }
}
```

Output:

```text
Song A
Song B
Song C
```

---

## 31. Encapsulation Boundary

It is okay for `PlaylistIterator` to know:

```java
String[] songs
```

because the iterator is part of the collection implementation.

What should stay hidden is:

```text
Client
   X
   |
   | should not depend on
   v
String[]
```

Internal relationship:

```text
Playlist
   |
   v
PlaylistIterator
   |
   v
String[]
```

is perfectly reasonable.

---

## 32. Interview Architecture Diagram

```text
              <<interface>>
                Iterable
                   |
                   | iterator()
                   v
              <<interface>>
                Iterator
             +-------------+
             | hasNext()   |
             | next()      |
             +------+------+
                    ^
                    |
             PlaylistIterator
                    |
                    | traverses
                    v
                Playlist
```

More conceptually:

```text
Client
   |
   v
Iterator
   |
   | traversal knowledge
   v
Collection
   |
   | storage knowledge
   v
Elements
```

---

## 33. Common Interview Questions

### Why Iterator?

To traverse a collection without exposing its internal representation.

### Where is traversal state stored?

Usually inside the Iterator.

### Why not store position in the collection?

Because multiple iterators should traverse independently.

### Iterable vs Iterator?

```text
Iterable
→ produces Iterator

Iterator
→ performs one traversal
```

### What happens when an ArrayList is structurally modified while iterating?

A standard fail-fast iterator may throw `ConcurrentModificationException`.

### Is Iterator only for lists?

No.

It also applies to:

```text
Trees
Graphs
Custom structures
File systems
Paginated sources
```

---

## 34. Common Interview Traps

### Trap 1
"Iterator is just a for-loop."

No.

It encapsulates traversal logic and traversal state.

### Trap 2
"Iterator means array/list only."

No.

It can hide complex tree/graph traversal.

### Trap 3
"Fail-fast means thread-safe."

No.

### Trap 4
"Iterable and Iterator are the same."

No.

```text
Iterable = can create traversal
Iterator = performs traversal
```

---

## 35. Interview Mental Model

If you hear:

> "I want to traverse this data structure without exposing how it is represented."

Think:

```text
Iterator Pattern
```

Core sentence:

> Iterator provides sequential access to elements of an aggregate without exposing its underlying representation, while keeping traversal state and traversal logic inside an iterator object.

---

## 36. Fast Revision Table

| Concept | Meaning |
|---|---|
| Iterator | Performs one traversal |
| Iterable | Produces an Iterator |
| `hasNext()` | Checks for more elements |
| `next()` | Returns next element |
| Traversal state | Stored inside Iterator |
| Multiple iterators | Independent traversal states |
| Fail-fast | Detects structural modification |
| Representation hiding | Client does not know storage |

---

## 37. One-Line Memory Aid

```text
Strategy = HOW

Command = WHAT ACTION

Observer = WHO GETS NOTIFIED

State = HOW BEHAVIOR CHANGES WITH STATE

Chain of Responsibility = WHO IN THE CHAIN PROCESSES / PASSES

Template Method = WHICH STEPS OF A FIXED WORKFLOW VARY

Iterator = HOW DO I TRAVERSE WITHOUT EXPOSING STORAGE
```
