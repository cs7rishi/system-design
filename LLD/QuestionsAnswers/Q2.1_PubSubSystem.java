import java.util.*;
import java.util.concurrent.*;

interface Event {
    String getType();
    String getData();
}

interface EventListener {
    void onEvent(Event event);
}

public class Q2.1_PubSubSystem {
    private final ConcurrentHashMap<String, Set<EventListener>> topicSubscribers = new ConcurrentHashMap<>();

    public void subscribe(String eventType, EventListener listener) {
        topicSubscribers.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(listener);
    }

    public void unsubscribe(String eventType, EventListener listener) {
        Set<EventListener> subs = topicSubscribers.get(eventType);
        if (subs != null) {
            subs.remove(listener);
        }
    }

    public void publish(Event event) {
        Set<EventListener> subs = topicSubscribers.get(event.getType());
        if (subs != null) {
            for (EventListener listener : subs) {
                // In production, dispatch asynchronously via executor
                listener.onEvent(event);
            }
        }
    }

    public int getSubscriberCount(String eventType) {
        Set<EventListener> subs = topicSubscribers.get(eventType);
        return subs == null ? 0 : subs.size();
    }
}