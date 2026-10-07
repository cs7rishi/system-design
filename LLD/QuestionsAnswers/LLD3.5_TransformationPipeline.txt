// Focus: Chain of Responsibility & Strategy Pattern
import java.util.List;

interface TransformationStrategy {
    Map<String, Object> transform(Map<String, Object> record);
}

class FilterStrategy implements TransformationStrategy {
    public Map<String, Object> transform(Map<String, Object> record) { /* logic */ return record; }
}

public class LLD3.5_TransformationPipeline {
    private List<TransformationStrategy> chain;

    public void processRecord(Map<String, Object> record) {
        for (TransformationStrategy step : chain) {
            record = step.transform(record);
            if (record == null) break; // Dropped by filter
        }
    }
}