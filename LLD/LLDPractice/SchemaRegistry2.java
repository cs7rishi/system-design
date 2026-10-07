import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 1. Core Models (Java 21 Records)
enum DataType { INT, STRING, BOOLEAN }

record Field(String name, DataType type, boolean required) {}

record Schema(int version, Map<String, Field> fields) {
    public Field getField(String name) { return fields.get(name); }
}

// 2. Compatibility Strategy Interface
interface CompatibilityChecker {
    boolean isCompatible(Schema newSchema, Schema prevSchema);
}

// BACKWARD: New schema can read old data.
// Rule: Any new field added in newSchema CANNOT be required. Existing field types cannot change.
class BackwardCompatibilityChecker implements CompatibilityChecker {
    public boolean isCompatible(Schema newSchema, Schema prevSchema) {
        for (Field newField : newSchema.fields().values()) {
            Field prevField = prevSchema.getField(newField.name());
            
            // New field introduced: must be optional
            if (prevField == null && newField.required()) {
                return false;
            }
            // Existing field: type cannot change
            if (prevField != null && prevField.type() != newField.type()) {
                return false;
            }
        }
        return true;
    }
}

// 3. Central Schema Registry
class SchemaRegistry {
    // Subject -> List of Versioned Schemas (index + 1 = version)
    private final Map<String, List<Schema>> store = new ConcurrentHashMap<>();
    private final CompatibilityChecker checker = new BackwardCompatibilityChecker();

    public synchronized Schema register(String subject, List<Field> fields) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        fields.forEach(f -> fieldMap.put(f.name(), f));

        List<Schema> history = store.computeIfAbsent(subject, k -> new ArrayList<>());

        if (!history.isEmpty()) {
            Schema latest = history.getLast();
            Schema candidate = new Schema(latest.version() + 1, fieldMap);

            if (!checker.isCompatible(candidate, latest)) {
                throw new IllegalArgumentException("Incompatible schema for subject: " + subject);
            }
        }

        Schema newSchema = new Schema(history.size() + 1, fieldMap);
        history.add(newSchema);
        return newSchema;
    }

    public Schema getLatest(String subject) {
        List<Schema> history = store.get(subject);
        if (history == null || history.isEmpty()) throw new NoSuchElementException("Subject not found");
        return history.getLast();
    }
}

// 4. Client Runner (Dry Run for the Interview Scenario)
public class ClientRunner {
    public static void main(String[] args) {
        SchemaRegistry registry = new SchemaRegistry();
        String subject = "user-event";

        // V1: {id: int, name: string}
        Schema v1 = registry.register(subject, List.of(
            new Field("id", DataType.INT, true),
            new Field("name", DataType.STRING, true)
        ));
        System.out.println("Registered V1: fields=" + v1.fields().keySet());

        // V2: Add optional email -> Allowed in BACKWARD
        Schema v2 = registry.register(subject, List.of(
            new Field("id", DataType.INT, true),
            new Field("name", DataType.STRING, true),
            new Field("email", DataType.STRING, false)
        ));
        System.out.println("Registered V2: fields=" + v2.fields().keySet());

        // V3: Make email required -> REJECTED in BACKWARD
        try {
            registry.register(subject, List.of(
                new Field("id", DataType.INT, true),
                new Field("name", DataType.STRING, true),
                new Field("email", DataType.STRING, true)
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("V3 Rejected as expected: " + e.getMessage());
        }
    }
}