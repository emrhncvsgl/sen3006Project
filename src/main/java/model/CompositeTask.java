package model;

import observer.TaskObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CompositeTask represents a composite node in the Composite pattern.
 * It can contain other tasks (both SimpleTask and CompositeTask).
 */
public class CompositeTask implements Task {
    private String name;
    private TaskStatus status;
    private LocalDateTime deadline;
    private final List<Task> children = new ArrayList<>();
    private final List<TaskObserver> observers = new ArrayList<>();

    /**
     * Constructs a CompositeTask with a name.
     * @param name The name of the task
     */
    public CompositeTask(String name) {
        this.name = name;
        this.status = TaskStatus.NOT_STARTED;
    }

    /**
     * Constructs a CompositeTask with a name and deadline.
     * @param name The name of the task
     * @param deadline The deadline for the task
     */
    public CompositeTask(String name, LocalDateTime deadline) {
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
        children.add(task);
    }

    @Override
    public void removeSubtask(Task task) {
        children.remove(task);
    }

    @Override
    public List<Task> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void setStatus(TaskStatus status) {
        // Check if status is changing to COMPLETED
        boolean isCompletionEvent = this.status != TaskStatus.COMPLETED && status == TaskStatus.COMPLETED;
        
        this.status = status;
        
        // Propagate status to all children
        if (status == TaskStatus.COMPLETED) {
            for (Task child : children) {
                child.setStatus(TaskStatus.COMPLETED);
            }
        }
        
        // Notify observers if the task is now complete
        if (isCompletionEvent) {
            notifyObservers("COMPLETED");
        }
    }

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Checks the status of all children and updates the composite task status accordingly.
     * @return The calculated status based on children
     */
    public TaskStatus calculateStatus() {
        if (children.isEmpty()) {
            return status;
        }

        boolean allCompleted = true;
        boolean anyInProgress = false;

        for (Task child : children) {
            TaskStatus childStatus = child.getStatus();
            if (childStatus != TaskStatus.COMPLETED) {
                allCompleted = false;
            }
            if (childStatus == TaskStatus.IN_PROGRESS) {
                anyInProgress = true;
            }
        }

        if (allCompleted) {
            return TaskStatus.COMPLETED;
        } else if (anyInProgress) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.NOT_STARTED;
        }
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
        
        // Also attach observer to all children
        for (Task child : children) {
            child.attachObserver(observer);
        }
    }

    @Override
    public void detachObserver(TaskObserver observer) {
        observers.remove(observer);
        
        // Also detach observer from all children
        for (Task child : children) {
            child.detachObserver(observer);
        }
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
