package Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private Map<EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();

    public EventManager() {
        Arrays.asList(EventType.values()).forEach(operation ->
            this.listeners.put(operation, new ArrayList<>()));
    }

    public void subscribe(EventType eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        users.add(listener);
    }

    public void unsubscribe(EventType eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        users.remove(listener);
    }

    public void notify(EventType eventType) {
        List<EventListener> users = listeners.get(eventType);
        for (EventListener listener : users) {
            listener.update(eventType);
        }
    }
}