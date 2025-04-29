package model;

import observer.TaskObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SimpleTask represents a leaf node in the Composite pattern.
 * It cannot contain other tasks.
 */
public class SimpleTask implements Task {
    private String name;
    private TaskStatus status;
    private LocalDateTime deadline;
    private final List<TaskObserver> observers = new ArrayList<>();

    /**
     * Constructs a SimpleTask with a name.
     * @param name The name of the task
     */
    public SimpleTask(String name) {
        this.name = name;
        this.status = TaskStatus.NOT_STARTED;
    }

    /**
     * Constructs a SimpleTask with a name and deadline.
     * @param name The name of the task
     * @param deadline The deadline for the task
     */
    public SimpleTask(String name, LocalDateTime deadline) {
        this(name);
        this.deadline = deadline;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addSubtask(Task task) {
        throw new UnsupportedOperationException("Cannot add subtask to a SimpleTask");
    }

    @Override
    public void removeSubtask(Task task) {
        throw new UnsupportedOperationException("Cannot remove subtask from a SimpleTask");
    }

    @Override
    public List<Task> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void setStatus(TaskStatus status) {
        // Check if status is changing to COMPLETED
        boolean isCompletionEvent = this.status != TaskStatus.COMPLETED && status == TaskStatus.COMPLETED;
        
        this.status = status;
        
        // Notify observers if the task is now complete
        if (isCompletionEvent) {
            notifyObservers("COMPLETED");
        }
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    @Override
    public LocalDateTime getDeadline() {
        return deadline;
    }

    @Override
    public void attachObserver(TaskObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detachObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String event) {
        for (TaskObserver observer : observers) {
            observer.update(this, event);
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
}
