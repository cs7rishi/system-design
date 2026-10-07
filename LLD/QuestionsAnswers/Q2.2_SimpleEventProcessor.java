import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Q2.2_SimpleEventProcessor {
    static class Event { long offset; String payload; }
    interface EventHandler { void handle(Event event) throws Exception; }
    interface OffsetStore { void saveOffset(long offset); long loadOffset(); }

    private final List<Event> eventSource;
    private final EventHandler handler;
    private final OffsetStore store;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private long lastProcessedOffset = -1;

    public Q2.2_SimpleEventProcessor(List<Event> eventSource, EventHandler handler, OffsetStore store) {
        this.eventSource = eventSource;
        this.handler = handler;
        this.store = store;
        this.lastProcessedOffset = store.loadOffset();
    }

    public void start() {
        isRunning.set(true);
        for (Event event : eventSource) {
            if (!isRunning.get()) break;
            if (event.offset <= lastProcessedOffset) continue; // Skip processed

            try {
                handler.handle(event);
                store.saveOffset(event.offset);
                lastProcessedOffset = event.offset;
            } catch (Exception e) {
                // Log and stop on failure to guarantee ordering
                System.err.println("Failed at offset: " + event.offset);
                break; 
            }
        }
    }

    public void stop() { isRunning.set(false); }
    public long getLastOffset() { return lastProcessedOffset; }
}