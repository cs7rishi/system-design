### Class Relationships & OO Design

1. IS-A Relationships (Interface Realization / Inheritance)
- FilterTransformation IS-A Transformation
- MapTransformation IS-A Transformation
- EnrichTransformation IS-A Transformation
- ValidateTransformation IS-A Transformation

2. HAS-A Relationships (Composition & Aggregation)
- TransformationPipeline HAS-A List<Transformation> (Aggregation: maintains an ordered pipeline chain)
- PipelineContext HAS-A List<FailedRecord> (Composition: owns the DLQ storage)
- FailedRecord HAS-A Record (Composition: binds failure reason with the failed record)
- Record HAS-A Map<String, Object> (Composition: encapsulates dynamic key-value payload)
- EnrichTransformation HAS-A Map<String, String> (Aggregation: holds lookup dictionary)

3. USES / DEPENDENCY Relationships
- ClientRunner USES TransformationPipeline, Record, PipelineContext
- TransformationPipeline USES PipelineContext (threads execution context across stages)
- Transformation USES Record, PipelineContext (accepts batch inputs and logs errors to context)

+-------------------------------------------------------+
|                       Record                          |
+-------------------------------------------------------+
| - data: Map<String, Object>                           |
+-------------------------------------------------------+
| + get(key: String): Object                            |
| + set(key: String, value: Object): void               |
| + remove(key: String): void                           |
| + contains(key: String): boolean                      |
| + getData(): Map<String, Object>                      |
+-------------------------------------------------------+
                           ^
                           | operates on
                           |
+-------------------------------------------------------+
|                   <<interface>>                       |
|                  Transformation                       |
+-------------------------------------------------------+
| + execute(records: List<Record>,                      |
|           ctx: PipelineContext): List<Record>         |
+-------------------------------------------------------+
         ^                     ^                     ^
         |                     |                     |
+------------------+  +------------------+  +------------------+
| FilterTransform  |  |   MapTransform   |  |  EnrichTransform |
+------------------+  +------------------+  +------------------+
| - predicate      |  | - mapper         |  | - sourceKey      |
|                  |  |                  |  | - targetKey      |
|                  |  |                  |  | - lookupTable    |
+------------------+  +------------------+  +------------------+

                           |
                           v uses
+-------------------------------------------------------+
|               TransformationPipeline                  |
+-------------------------------------------------------+
| - stages: List<Transformation>                        |
+-------------------------------------------------------+
| + add(stage: Transformation): TransformationPipeline  |
| + run(records: List<Record>,                          |
|       ctx: PipelineContext): List<Record>             |
+-------------------------------------------------------+
                           |
                           v records errors to
+-------------------------------------------------------+
|                   PipelineContext                     |
+-------------------------------------------------------+
| - dlq: List<FailedRecord>                             |
+-------------------------------------------------------+
| + toDLQ(record: Record, reason: String): void         |
| + getDlq(): List<FailedRecord>                        |
+-------------------------------------------------------+
                           |
                           v contains
+-------------------------------------------------------+
|                     FailedRecord                      |
+-------------------------------------------------------+
| - record: Record                                      |
| - reason: String                                      |
+-------------------------------------------------------+

---------------------------------------------------------------------------

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

// ==========================================
// 1. Core Domain Models & DLQ
// ==========================================
class Record {
    private final Map<String, Object> data;

    public Record(Map<String, Object> data) {
        this.data = new HashMap<>(data);
    }

    public Object get(String key) { return data.get(key); }
    public void set(String key, Object val) { data.put(key, val); }
    public void remove(String key) { data.remove(key); }
    public boolean contains(String key) { return data.containsKey(key); }
    public Map<String, Object> getData() { return Collections.unmodifiableMap(data); }

    @Override
    public String toString() { return data.toString(); }
}

record FailedRecord(Record record, String reason) {}

class PipelineContext {
    private final List<FailedRecord> dlq = new ArrayList<>();

    public void toDLQ(Record record, String reason) {
        dlq.add(new FailedRecord(record, reason));
    }

    public List<FailedRecord> getDlq() {
        return Collections.unmodifiableList(dlq);
    }
}

// ==========================================
// 2. Transformation Strategy Interface
// ==========================================
interface Transformation {
    List<Record> execute(List<Record> records, PipelineContext ctx);
}

// ==========================================
// 3. Concrete Implementations
// ==========================================
class FilterTransformation implements Transformation {
    private final Predicate<Record> predicate;

    public FilterTransformation(Predicate<Record> predicate) {
        this.predicate = predicate;
    }

    @Override
    public List<Record> execute(List<Record> records, PipelineContext ctx) {
        List<Record> out = new ArrayList<>();
        for (Record r : records) {
            try {
                if (predicate.test(r)) {
                    out.add(r);
                }
            } catch (Exception e) {
                ctx.toDLQ(r, "FILTER_FAIL: " + e.getMessage());
            }
        }
        return out;
    }
}

class MapTransformation implements Transformation {
    private final Consumer<Record> mapper;

    public MapTransformation(Consumer<Record> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Record> execute(List<Record> records, PipelineContext ctx) {
        List<Record> out = new ArrayList<>();
        for (Record r : records) {
            try {
                mapper.accept(r);
                out.add(r);
            } catch (Exception e) {
                ctx.toDLQ(r, "MAP_FAIL: " + e.getMessage());
            }
        }
        return out;
    }
}

class EnrichTransformation implements Transformation {
    private final String sourceKey;
    private final String targetKey;
    private final Map<String, String> lookupTable;

    public EnrichTransformation(String sourceKey, String targetKey, Map<String, String> lookupTable) {
        this.sourceKey = sourceKey;
        this.targetKey = targetKey;
        this.lookupTable = lookupTable;
    }

    @Override
    public List<Record> execute(List<Record> records, PipelineContext ctx) {
        List<Record> out = new ArrayList<>();
        for (Record r : records) {
            Object val = r.get(sourceKey);
            if (val != null && lookupTable.containsKey(val.toString())) {
                r.set(targetKey, lookupTable.get(val.toString()));
                out.add(r);
            } else {
                ctx.toDLQ(r, "ENRICH_NOT_FOUND: key=" + val);
            }
        }
        return out;
    }
}

class ValidateTransformation implements Transformation {
    private final List<String> requiredFields;

    public ValidateTransformation(List<String> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public List<Record> execute(List<Record> records, PipelineContext ctx) {
        List<Record> out = new ArrayList<>();
        for (Record r : records) {
            boolean isValid = true;
            for (String field : requiredFields) {
                if (!r.contains(field) || r.get(field) == null) {
                    ctx.toDLQ(r, "MISSING_REQUIRED_FIELD: " + field);
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                out.add(r);
            }
        }
        return out;
    }
}

// ==========================================
// 4. Pipeline Chain
// ==========================================
class TransformationPipeline {
    private final List<Transformation> stages = new ArrayList<>();

    public TransformationPipeline add(Transformation t) {
        stages.add(t);
        return this;
    }

    public List<Record> run(List<Record> input, PipelineContext ctx) {
        List<Record> current = input;
        for (Transformation stage : stages) {
            if (current.isEmpty()) break;
            current = stage.execute(current, ctx);
        }
        return current;
    }
}

// ==========================================
// 5. Client Test Runner (Example Scenario)
// ==========================================
public class ClientRunner {
    public static void main(String[] args) {
        // Step 1: Prepare Input Records
        List<Record> inputRecords = List.of(
            new Record(Map.of("id", 1, "name", "John", "age", 25, "country_code", "US")),
            new Record(Map.of("id", 2, "name", "Jane", "age", 17, "country_code", "UK")),
            new Record(Map.of("id", 3, "name", "Bob", "age", 30, "country_code", "CA"))
        );

        // Step 2: Enrichment Lookup Table (Mock for API/DB)
        Map<String, String> countryCodeMap = Map.of(
            "US", "United States",
            "CA", "Canada",
            "UK", "United Kingdom"
        );

        // Step 3: Build Pipeline via Fluent Interface
        TransformationPipeline pipeline = new TransformationPipeline()
            // Stage 1: Filter (age >= 18) -> Jane (17) is excluded
            .add(new FilterTransformation(r -> ((Integer) r.get("age")) >= 18))

            // Stage 2: Map (Rename country_code -> country & Compute age_group)
            .add(new MapTransformation(r -> {
                r.set("country", r.get("country_code"));
                r.remove("country_code");

                int age = (Integer) r.get("age");
                r.set("age_group", age < 30 ? "young" : "senior");
            }))

            // Stage 3: Enrich (US -> United States, CA -> Canada)
            .add(new EnrichTransformation("country", "country", countryCodeMap))

            // Stage 4: Validate required fields
            .add(new ValidateTransformation(List.of("id", "name", "age", "country", "age_group")));

        // Step 4: Execute
        PipelineContext context = new PipelineContext();
        List<Record> output = pipeline.run(inputRecords, context);

        // Step 5: Verify Outputs
        System.out.println("=== Successful Records (" + output.size() + ") ===");
        output.forEach(System.out::println);

        System.out.println("\n=== Dead Letter Queue (" + context.getDlq().size() + ") ===");
        for (FailedRecord failed : context.getDlq()) {
            System.out.println("Failed: " + failed.record() + " | Reason: " + failed.reason());
        }
    }
}