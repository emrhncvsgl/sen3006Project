package observer;

import model.Task;

/**
 * TaskObserver interface representing the Observer in the Observer pattern.
 */
public interface TaskObserver {
    /**
     * Update method called by the subject (Task) when a state change occurs.
     * @param task The task that has changed
     * @param event The event type that occurred
     */
    void update(Task task, String event);
}
