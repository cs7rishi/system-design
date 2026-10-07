import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Q1.2_EventStreamProcessor {
    public enum EventType { BEGIN, COMMIT, ROLLBACK, INSERT, UPDATE, DELETE }
    
    public static class Event {
        EventType type;
        long txnId;
        String data;
        
        public Event(EventType type, long txnId, String data) {
            this.type = type; this.txnId = txnId; this.data = data;
        }
    }

    private final Map<Long, List<Event>> transactionBuffers = new ConcurrentHashMap<>();

    public void processEvent(Event event) {
        switch (event.type) {
            case BEGIN:
                transactionBuffers.putIfAbsent(event.txnId, new ArrayList<>());
                break;
            case COMMIT:
                List<Event> committedEvents = transactionBuffers.remove(event.txnId);
                if (committedEvents != null) {
                    flushToOutput(event.txnId, committedEvents);
                }
                break;
            case ROLLBACK:
                transactionBuffers.remove(event.txnId);
                break;
            case INSERT:
            case UPDATE:
            case DELETE:
                transactionBuffers.computeIfPresent(event.txnId, (id, buffer) -> {
                    buffer.add(event);
                    return buffer;
                });
                break;
        }
    }

    private void flushToOutput(long txnId, List<Event> events) {
        System.out.println("Transaction " + txnId + " committed: " + events.size() + " events.");
        // Downstream processing logic goes here
    }
}