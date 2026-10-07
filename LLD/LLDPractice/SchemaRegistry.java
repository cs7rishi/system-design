+-------------------------------------------------------+
|                   FieldDefinition                     |  (Java 21 Record)
+-------------------------------------------------------+
| - name: String                                        |
| - type: DataType (INT, STRING, etc.)                  |
| - required: boolean                                   |
| - defaultValue: Object                                |
+-------------------------------------------------------+
                           ^
                           | 1..* (contains)
+-------------------------------------------------------+
|                       Schema                          |  (Java 21 Record)
+-------------------------------------------------------+
| - id: int                                             |
| - subject: String                                     |
| - version: int                                        |
| - fields: Map<String, FieldDefinition>                |
+-------------------------------------------------------+
| + getField(name: String): FieldDefinition             |
+-------------------------------------------------------+
                           ^
                           | validates / compares
+-------------------------------------------------------+
|                 <<interface>>                         |
|              CompatibilityChecker                     |  (Strategy Pattern)
+-------------------------------------------------------+
| + isCompatible(newSchema: Schema,                     |
|                prevSchema: Schema): boolean           |
+-------------------------------------------------------+
         ^                     ^                     ^
         |                     |                     |
+------------------+  +------------------+  +------------------+
| BackwardChecker  |  |  ForwardChecker  |  |   FullChecker    |
+------------------+  +------------------+  +------------------+

+-------------------------------------------------------+
|                 <<interface>>                         |
|                  SchemaStore                          |  (Repository Pattern)
+-------------------------------------------------------+
| + save(schema: Schema): Schema                        |
| + getLatest(subject: String): Optional<Schema>        |
| + getByVersion(subject: String, ver: int): Schema     |
+-------------------------------------------------------+
         ^
         | implements
+-------------------------------------------------------+
|              InMemorySchemaStore                      |
+-------------------------------------------------------+
| - storage: ConcurrentMap<String, List<Schema>>        |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                 SchemaRegistry                        |  (Façade / Core Engine)
+-------------------------------------------------------+
| - store: SchemaStore                                  |
| - cache: Map<String, Schema>                          |
| - modes: Map<String, CompatibilityMode>               |
+-------------------------------------------------------+
| + register(subject: String, fields: List<Field>): int |
| + getLatest(subject: String): Schema                  |
| + setCompatibilityMode(sub: String, mode: Mode): void |
+-------------------------------------------------------+


1. IS-A Relationships (Interface Realization / Inheritance)
- BackwardCompatibilityChecker IS-A CompatibilityChecker
- ForwardCompatibilityChecker  IS-A CompatibilityChecker
- FullCompatibilityChecker     IS-A CompatibilityChecker
- NoneCompatibilityChecker     IS-A CompatibilityChecker
- InMemorySchemaStore          IS-A SchemaStore

2. HAS-A Relationships (Composition & Aggregation)
- Schema HAS-A List<FieldDefinition> (Composition: a schema is defined by its immutable fields)
- SchemaMetadata HAS-A CompatibilityMode (Composition: subject-level evolution policy)
- SchemaRegistry HAS-A SchemaStore (Aggregation: decoupled persistence engine)
- SchemaRegistry HAS-A CompatibilityCheckerFactory (Aggregation: resolves checker strategy)
- SchemaRegistry HAS-A Cache (Composition: O(1) in-memory lookup cache)
- MigrationResult HAS-A List<String> (Composition: holds generated DDL statements)

3. USES / DEPENDENCY Relationships
- ClientRunner USES SchemaRegistry, Schema, FieldDefinition
- CompatibilityChecker USES Schema (validates target schema against previous schema)
- MigrationEngine USES Schema (computes DDL diff between two schemas)


----------------------------------------------------------------------

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// ==========================================
// 1. Domain Models (Java 21 Records)
// ==========================================
enum DataType { INT, STRING, BOOLEAN, DOUBLE }

enum CompatibilityMode { NONE, BACKWARD, FORWARD, FULL }

record FieldDefinition(String name, DataType type, boolean required, Object defaultValue) {
    public boolean hasDefault() {
        return defaultValue != null;
    }
}

record Schema(int id, String subject, int version, Map<String, FieldDefinition> fields) {
    public FieldDefinition getField(String name) {
        return fields.get(name);
    }
}

record FailedValidation(String fieldName, String reason) {}

// ==========================================
// 2. Compatibility Strategy (Strategy Pattern)
// ==========================================
interface CompatibilityChecker {
    boolean isCompatible(Schema newSchema, Schema prevSchema);
}

class BackwardCompatibilityChecker implements CompatibilityChecker {
    // BACKWARD: New schema can read data written with prev schema.
    // Rule: Every field required in newSchema MUST already exist in prevSchema (or have a default value).
    // Types of existing fields must remain identical.
    @Override
    public boolean isCompatible(Schema newSchema, Schema prevSchema) {
        for (FieldDefinition newField : newSchema.fields().values()) {
            FieldDefinition prevField = prevSchema.getField(newField.name());
            
            if (prevField == null) {
                // New field added: Must NOT be strictly required without a default value
                if (newField.required() && !newField.hasDefault()) {
                    return false;
                }
            } else {
                // Existing field: Type cannot mutate
                if (newField.type() != prevField.type()) {
                    return false;
                }
            }
        }
        return true;
    }
}

class ForwardCompatibilityChecker implements CompatibilityChecker {
    // FORWARD: Prev schema can read data written with new schema.
    // Rule: Prev schema's required fields must still exist in newSchema.
    @Override
    public boolean isCompatible(Schema newSchema, Schema prevSchema) {
        for (FieldDefinition prevField : prevSchema.fields().values()) {
            FieldDefinition newField = newSchema.getField(prevField.name());
            if (newField == null && prevField.required()) {
                return false; // Dropping a required field breaks forward reader
            }
            if (newField != null && newField.type() != prevField.type()) {
                return false;
            }
        }
        return true;
    }
}

class FullCompatibilityChecker implements CompatibilityChecker {
    private final CompatibilityChecker backward = new BackwardCompatibilityChecker();
    private final CompatibilityChecker forward = new ForwardCompatibilityChecker();

    @Override
    public boolean isCompatible(Schema newSchema, Schema prevSchema) {
        return backward.isCompatible(newSchema, prevSchema) && forward.isCompatible(newSchema, prevSchema);
    }
}

class NoneCompatibilityChecker implements CompatibilityChecker {
    @Override
    public boolean isCompatible(Schema newSchema, Schema prevSchema) {
        return true;
    }
}

// ==========================================
// 3. Storage Layer (Repository Pattern)
// ==========================================
interface SchemaStore {
    Schema save(String subject, Map<String, FieldDefinition> fields);
    Optional<Schema> getLatest(String subject);
    Optional<Schema> getByVersion(String subject, int version);
    Optional<Schema> getById(int id);
}

class InMemorySchemaStore implements SchemaStore {
    private final AtomicInteger idGenerator = new AtomicInteger(100);
    private final Map<String, List<Schema>> subjectSchemas = new ConcurrentHashMap<>();
    private final Map<Integer, Schema> idIndex = new ConcurrentHashMap<>();

    @Override
    public synchronized Schema save(String subject, Map<String, FieldDefinition> fields) {
        List<Schema> versions = subjectSchemas.computeIfAbsent(subject, k -> new ArrayList<>());
        int nextVersion = versions.size() + 1;
        int schemaId = idGenerator.incrementAndGet();

        Schema schema = new Schema(schemaId, subject, nextVersion, Map.copyOf(fields));
        versions.add(schema);
        idIndex.put(schemaId, schema);
        return schema;
    }

    @Override
    public Optional<Schema> getLatest(String subject) {
        List<Schema> versions = subjectSchemas.get(subject);
        if (versions == null || versions.isEmpty()) return Optional.empty();
        return Optional.of(versions.getLast());
    }

    @Override
    public Optional<Schema> getByVersion(String subject, int version) {
        List<Schema> versions = subjectSchemas.get(subject);
        if (versions == null || version <= 0 || version > versions.size()) return Optional.empty();
        return Optional.of(versions.get(version - 1));
    }

    @Override
    public Optional<Schema> getById(int id) {
        return Optional.ofNullable(idIndex.get(id));
    }
}

// ==========================================
// 4. Migration Script Engine
// ==========================================
class MigrationEngine {
    public static List<String> generateDdl(String tableName, Schema oldSchema, Schema newSchema) {
        List<String> ddlStatements = new ArrayList<>();
        
        // Detect newly added columns
        for (FieldDefinition field : newSchema.fields().values()) {
            if (!oldSchema.fields().containsKey(field.name())) {
                String ddl = String.format("ALTER TABLE %s ADD COLUMN %s %s%s;",
                        tableName,
                        field.name(),
                        field.type(),
                        field.required() ? " NOT NULL" : "");
                ddlStatements.add(ddl);
            }
        }
        
        // Detect dropped columns
        for (FieldDefinition field : oldSchema.fields().values()) {
            if (!newSchema.fields().containsKey(field.name())) {
                ddlStatements.add(String.format("ALTER TABLE %s DROP COLUMN %s;", tableName, field.name()));
            }
        }
        return ddlStatements;
    }
}

// ==========================================
// 5. Schema Registry Façade & Cache
// ==========================================
class SchemaRegistry {
    private final SchemaStore store;
    private final Map<String, CompatibilityMode> subjectModes = new ConcurrentHashMap<>();
    
    // Fast O(1) Cache: "subject:version" -> Schema
    private final Map<String, Schema> cache = new ConcurrentHashMap<>();

    public SchemaRegistry(SchemaStore store) {
        this.store = store;
    }

    public void setCompatibilityMode(String subject, CompatibilityMode mode) {
        subjectModes.put(subject, mode);
    }

    public CompatibilityMode getMode(String subject) {
        return subjectModes.getOrDefault(subject, CompatibilityMode.BACKWARD);
    }

    private CompatibilityChecker resolveChecker(CompatibilityMode mode) {
        return switch (mode) {
            case BACKWARD -> new BackwardCompatibilityChecker();
            case FORWARD  -> new ForwardCompatibilityChecker();
            case FULL     -> new FullCompatibilityChecker();
            case NONE     -> new NoneCompatibilityChecker();
        };
    }

    public Schema register(String subject, List<FieldDefinition> fieldsList) {
        Map<String, FieldDefinition> fieldMap = new LinkedHashMap<>();
        for (FieldDefinition f : fieldsList) {
            fieldMap.put(f.name(), f);
        }

        Optional<Schema> latestOpt = store.getLatest(subject);

        if (latestOpt.isPresent()) {
            Schema latest = latestOpt.get();
            // Candidate schema representation for compatibility evaluation
            Schema candidate = new Schema(0, subject, latest.version() + 1, fieldMap);
            
            CompatibilityMode mode = getMode(subject);
            CompatibilityChecker checker = resolveChecker(mode);

            if (!checker.isCompatible(candidate, latest)) {
                throw new IllegalArgumentException(String.format(
                    "Schema registration rejected for subject '%s': Violates %s compatibility.", subject, mode
                ));
            }
        }

        Schema saved = store.save(subject, fieldMap);
        cache.put(cacheKey(subject, saved.version()), saved);
        return saved;
    }

    public Schema getSchema(String subject, int version) {
        String key = cacheKey(subject, version);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Schema schema = store.getByVersion(subject, version)
                .orElseThrow(() -> new NoSuchElementException("Schema not found: " + key));
        cache.put(key, schema);
        return schema;
    }

    public Schema getLatest(String subject) {
        return store.getLatest(subject)
                .orElseThrow(() -> new NoSuchElementException("Subject has no schemas: " + subject));
    }

    private String cacheKey(String subject, int version) {
        return subject + ":" + version;
    }
}

// ==========================================
// 6. Client Runner (Executing the Scenario)
// ==========================================
public class ClientRunner {
    public static void main(String[] args) {
        SchemaRegistry registry = new SchemaRegistry(new InMemorySchemaStore());
        String subject = "user-event";
        registry.setCompatibilityMode(subject, CompatibilityMode.BACKWARD);

        System.out.println("--- 1. Registering V1 ---");
        List<FieldDefinition> v1Fields = List.of(
            new FieldDefinition("id", DataType.INT, true, null),
            new FieldDefinition("name", DataType.STRING, true, null)
        );
        Schema v1 = registry.register(subject, v1Fields);
        System.out.println("Registered V1: id=" + v1.id() + ", version=" + v1.version() + ", fields=" + v1.fields().keySet());

        System.out.println("\n--- 2. Registering V2 (Add optional 'email') ---");
        List<FieldDefinition> v2Fields = List.of(
            new FieldDefinition("id", DataType.INT, true, null),
            new FieldDefinition("name", DataType.STRING, true, null),
            new FieldDefinition("email", DataType.STRING, false, null) // optional -> allowed in BACKWARD
        );
        Schema v2 = registry.register(subject, v2Fields);
        System.out.println("Registered V2: id=" + v2.id() + ", version=" + v2.version() + ", fields=" + v2.fields().keySet());

        System.out.println("\n--- 3. Migration DDL: V1 -> V2 ---");
        List<String> ddl = MigrationEngine.generateDdl("users", v1, v2);
        ddl.forEach(System.out::println);

        System.out.println("\n--- 4. Registering V3 (Make 'email' required without default) ---");
        List<FieldDefinition> v3Fields = List.of(
            new FieldDefinition("id", DataType.INT, true, null),
            new FieldDefinition("name", DataType.STRING, true, null),
            new FieldDefinition("email", DataType.STRING, true, null) // required without default -> breaks BACKWARD
        );

        try {
            registry.register(subject, v3Fields);
        } catch (IllegalArgumentException ex) {
            System.err.println("Expected rejection: " + ex.getMessage());
        }

        System.out.println("\n--- 5. Cache Verification ---");
        Schema cachedV1 = registry.getSchema(subject, 1);
        System.out.println("Resolved from cache: " + cachedV1.subject() + " v" + cachedV1.version());
    }
}