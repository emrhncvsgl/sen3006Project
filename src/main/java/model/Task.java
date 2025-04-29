package model;

import observer.TaskObserver;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task interface representing the Component in the Composite pattern.
 * Also serves as the Subject in the Observer pattern.
 */
public interface Task {
    /**
     * Gets the name of the task.
     * @return The task name
     */
    String getName();

    /**
     * Sets the name of the task.
     * @param name The task name to set
     */
    void setName(String name);

    /**
     * Add a subtask to this task.
     * @param task The subtask to add
     * @throws UnsupportedOperationException if the operation is not supported
     */
    void addSubtask(Task task);

    /**
     * Remove a subtask from this task.
     * @param task The subtask to remove
     * @throws UnsupportedOperationException if the operation is not supported
     */
    void removeSubtask(Task task);

    /**
     * Get all child tasks of this task.
     * @return List of child tasks
     */
    List<Task> getChildren();

    /**
     * Sets the status of the task.
     * @param status The new status
     */
    void setStatus(TaskStatus status);

    /**
     * Gets the current status of the task.
     * @return The current status
     */
    TaskStatus getStatus();

    /**
     * Sets the deadline for the task.
     * @param deadline The deadline date and time
     */
    void setDeadline(LocalDateTime deadline);

    /**
     * Gets the deadline of the task.
     * @return The deadline date and time
     */
    LocalDateTime getDeadline();

    /**
     * Add an observer to this task.
     * @param observer The observer to add
     */
    void attachObserver(TaskObserver observer);

    /**
     * Remove an observer from this task.
     * @param observer The observer to remove
     */
    void detachObserver(TaskObserver observer);

    /**
     * Notify all observers of a state change.
     * @param event The event type that occurred
     */
    void notifyObservers(String event);
}
