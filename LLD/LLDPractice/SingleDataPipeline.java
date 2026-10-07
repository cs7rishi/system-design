+-------------------------------------------------------+
|                        Record                         | (Java 21 Record)
+-------------------------------------------------------+
| - id: String                                          |
| - data: Map<String, Object>                           |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                 <<interface>>                         |
|                 SourceReader                          |
+-------------------------------------------------------+
| + readBatch(offset: long, batchSize: int): List<Rec>  |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                 <<interface>>                         |
|             TransformationEngine                      |
+-------------------------------------------------------+
| + transform(record: Record): Record                   |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                 <<interface>>                         |
|               DestinationWriter                       |
+-------------------------------------------------------+
| + writeBatch(records: List<Record>): void <<throws>>  |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                 <<interface>>                         |
|               CheckpointManager                       |
+-------------------------------------------------------+
| + commitOffset(offset: long): void                    |
| + getLastOffset(): long                               |
+-------------------------------------------------------+

+-------------------------------------------------------+
|                  RetryHandler                         | (Utility)
+-------------------------------------------------------+
| + executeWithBackoff(maxRetries: int,                 |
|                      action: Runnable): boolean       |
+-------------------------------------------------------+

+-------------------------------------------------------+
|               PipelineProcessor                       | (Core Engine)
+-------------------------------------------------------+
| - reader: SourceReader                                |
| - transformer: TransformationEngine                   |
| - writer: DestinationWriter                           |
| - checkpointer: CheckpointManager                     |
| - dlq: DeadLetterQueue                                |
+-------------------------------------------------------+
| + start(): void                                       |
+-------------------------------------------------------+


1. IS-A Relationships (Interface Realization)
- (Implementations of the core interfaces below are injected at runtime)
- PostgresReader       IS-A SourceReader
- SnowflakeWriter      IS-A DestinationWriter
- RedisCheckpointStore IS-A CheckpointManager

2. HAS-A Relationships (Composition & Aggregation)
- PipelineProcessor HAS-A SourceReader (Aggregation: reads data)
- PipelineProcessor HAS-A TransformationEngine (Aggregation: applies business logic)
- PipelineProcessor HAS-A DestinationWriter (Aggregation: sinks data)
- PipelineProcessor HAS-A CheckpointManager (Aggregation: state recovery)
- PipelineProcessor HAS-A DeadLetterQueue (Aggregation: error handling)
- PipelineProcessor HAS-A RetryHandler (Composition: handles transient failures)

3. USES / DEPENDENCY Relationships
- RetryHandler USES Runnable/Callable (executes the destination write block)
- ClientRunner USES PipelineProcessor to orchestrate the ETL flow



------------------------------------------------------------------------------------------------------


import java.util.*;

// ==========================================
// 1. Data Model
// ==========================================
record Record(String id, Map<String, Object> data) {}

// ==========================================
// 2. Core Component Interfaces
// ==========================================
// REQUIREMENT: Reads data from source (database, file, queue)
interface SourceReader {
    List<Record> readBatch(long offset, int batchSize);
}

// REQUIREMENT: Applies data transformations
interface TransformationEngine {
    Record transform(Record record) throws Exception;
}

// REQUIREMENT: Writes processed data to destination
interface DestinationWriter {
    void writeBatch(List<Record> records) throws Exception;
}

// REQUIREMENT: Manages offset/position tracking for Crash Recovery
interface CheckpointManager {
    void commitOffset(long offset);
    long getLastOffset();
}

// REQUIREMENT: Error queue for failed records (DLQ)
interface DeadLetterQueue {
    void send(Record record, String reason);
    void sendBatch(List<Record> records, String reason);
}

// ==========================================
// 3. Retry Strategy Engine
// ==========================================
// REQUIREMENT: Handles failures with exponential backoff (1s, 2s, 4s, 8s)
class RetryHandler {
    public static boolean executeWithBackoff(int maxRetries, Runnable action) {
        long waitTimeMs = 1000; // Start with 1 second

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                action.run();
                return true; // Success, exit retry loop
            } catch (Exception e) {
                System.err.println("[RetryHandler] Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) {
                    break;
                }
                try {
                    Thread.sleep(waitTimeMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                waitTimeMs *= 2; // Exponential backoff (1s -> 2s -> 4s -> 8s)
            }
        }
        return false; // All retries exhausted
    }
}

// ==========================================
// 4. Main Pipeline Processor (The Orchestrator)
// ==========================================
class PipelineProcessor {
    private final SourceReader reader;
    private final TransformationEngine transformer;
    private final DestinationWriter writer;
    private final CheckpointManager checkpointer;
    private final DeadLetterQueue dlq;
    private final int batchSize;
    private final int maxRetries;

    public PipelineProcessor(SourceReader reader, TransformationEngine transformer,
                             DestinationWriter writer, CheckpointManager checkpointer,
                             DeadLetterQueue dlq, int batchSize, int maxRetries) {
        this.reader = reader;
        this.transformer = transformer;
        this.writer = writer;
        this.checkpointer = checkpointer;
        this.dlq = dlq;
        this.batchSize = batchSize;
        this.maxRetries = maxRetries;
    }

    public void start() {
        // REQUIREMENT: Recovery scenario -> On restart, read checkpoint, resume from offset
        long currentOffset = checkpointer.getLastOffset();
        System.out.println("Starting pipeline from offset: " + currentOffset);

        while (true) {
            // 1. READ BATCH
            List<Record> batch;
            try {
                batch = reader.readBatch(currentOffset, batchSize);
                if (batch.isEmpty()) {
                    System.out.println("No more records. Pipeline idling/stopping.");
                    break;
                }
            } catch (Exception e) {
                System.err.println("Fatal Source Error. Halting pipeline to prevent data loss.");
                break; // Circuit breaker pattern on Source read failure
            }

            // 2. TRANSFORM (Handling Partial Batch Failures)
            List<Record> validRecords = new ArrayList<>();
            for (Record record : batch) {
                try {
                    // REQUIREMENT: Transform errors (invalid data, business logic failures)
                    Record transformed = transformer.transform(record);
                    validRecords.add(transformed);
                } catch (Exception e) {
                    // REQUIREMENT: Partial batch failures (some succeed, some fail due to schema)
                    dlq.send(record, "TRANSFORM_ERROR: " + e.getMessage());
                }
            }

            // 3. WRITE TO DESTINATION (With Retry & Backoff)
            if (!validRecords.isEmpty()) {
                // Wrap the fragile network call in our RetryHandler
                boolean success = RetryHandler.executeWithBackoff(maxRetries, () -> {
                    try {
                        writer.writeBatch(validRecords);
                    } catch (Exception e) {
                        throw new RuntimeException(e); // Trigger retry
                    }
                });

                // REQUIREMENT: Failure scenario -> After 5 retries, store in error queue, skip batch
                if (!success) {
                    System.err.println("Destination unreachable after " + maxRetries + " retries.");
                    dlq.sendBatch(validRecords, "DESTINATION_WRITE_FAILED_AFTER_RETRIES");
                }
            }

            // 4. CHECKPOINT PROGRESS
            // REQUIREMENT: Checkpoint offset 3000 (skip failed batch for now), continue processing
            // REQUIREMENT: Exactly-once processing semantics (advancing offset ensures we don't re-read)
            currentOffset += batch.size();
            checkpointer.commitOffset(currentOffset);
            System.out.println("Committed Checkpoint at offset: " + currentOffset);
        }
    }
}

// ==========================================
// 5. Client Runner (Mocking the Example Scenario)
// ==========================================
public class ClientRunner {
    public static void main(String[] args) {
        // Mock Source (Simulates returning 1000 records, then stopping)
        SourceReader mockReader = (offset, size) -> {
            if (offset >= 3000) return Collections.emptyList(); // End of data
            List<Record> batch = new ArrayList<>();
            for(int i = 0; i < size; i++) {
                batch.add(new Record("ID-" + (offset + i), Map.of("val", i)));
            }
            return batch;
        };

        // Mock Transformer (Fails specifically on record ID-2005 to show partial failure)
        TransformationEngine mockTransformer = record -> {
            if (record.id().equals("ID-2005")) {
                throw new IllegalArgumentException("Invalid JSON Schema format");
            }
            return record;
        };

        // Mock Destination (Fails constantly on offset 2000 to trigger Exponential Backoff)
        DestinationWriter mockWriter = records -> {
            if (records.get(0).id().equals("ID-2000")) {
                throw new RuntimeException("503 Service Unavailable");
            }
            System.out.println("Successfully wrote " + records.size() + " records to destination.");
        };

        // Mock Checkpointer (Simulates a restart from offset 1000)
        CheckpointManager mockCheckpointer = new CheckpointManager() {
            private long offset = 1000;
            public void commitOffset(long o) { this.offset = o; }
            public long getLastOffset() { return offset; }
        };

        // Mock DLQ
        DeadLetterQueue mockDlq = new DeadLetterQueue() {
            public void send(Record record, String reason) {
                System.out.println("[DLQ-SINGLE] " + record.id() + " -> " + reason);
            }
            public void sendBatch(List<Record> records, String reason) {
                System.out.println("[DLQ-BATCH] Sent " + records.size() + " records to DLQ -> " + reason);
            }
        };

        // Initialize and Start Engine
        PipelineProcessor engine = new PipelineProcessor(
                mockReader, mockTransformer, mockWriter, mockCheckpointer, mockDlq, 1000, 3
        );
        
        engine.start();
    }
}