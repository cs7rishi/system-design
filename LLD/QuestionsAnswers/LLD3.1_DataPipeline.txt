// Focus: Reader-Transformer-Writer pattern, Retry, Checkpointing
public interface SourceReader<T> { List<T> readBatch(); }
public interface DestinationWriter<T> { void writeBatch(List<T> data) throws Exception; }
public interface CheckpointManager { void commitOffset(long offset); }

public class LLD3.1_DataPipeline<T> {
    private SourceReader<T> reader;
    private DestinationWriter<T> writer;
    private Q2.6_RetryPolicy retryPolicy;
    private CheckpointManager checkpointManager;

    public void runPipeline() {
        List<T> batch = reader.readBatch();
        while(!batch.isEmpty()) {
            try {
                retryPolicy.execute(() -> {
                    writer.writeBatch(batch);
                    return null;
                });
                // Calculate and commit offset
                checkpointManager.commitOffset(100); 
            } catch (Exception e) {
                // Route to Dead Letter Queue
            }
            batch = reader.readBatch();
        }
    }
}