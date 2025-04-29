# Task Management Application

A JavaFX desktop application that demonstrates the Composite and Observer design patterns for organizing and tracking tasks.

## Design Patterns

### Composite Pattern

The Composite pattern is implemented through the task hierarchy:

- **Task Interface**: Defines the common contract for all task types (Component)
- **SimpleTask**: Represents leaf nodes that cannot contain subtasks (Leaf)
- **CompositeTask**: Represents composite nodes that can contain subtasks (Composite)

This pattern allows treating individual tasks and task groups uniformly, enabling operations like status updates to cascade through the entire hierarchy. The composite structure is visualized in the TreeView, where users can dynamically add, remove, and nest tasks.

### Observer Pattern

The Observer pattern is implemented to notify users of important task events:

- **TaskObserver Interface**: Defines the update method for all observers (Observer)
- **DeadlineNotifier**: Observes tasks and notifies when deadlines are approaching (Concrete Observer)
- **TaskCompletionNotifier**: Observes tasks and notifies when they are completed (Concrete Observer)
- **Task Interface**: Includes methods for attaching/detaching observers (Subject)

Each task acts as a subject that notifies its observers of state changes, enabling real-time updates without tight coupling between components. This pattern facilitates extensibility, as new notification types can be added without modifying the task classes.

## Running the Application

To run the application:

1 - Install maven if not installed on your pc

```bash
mvn javafx:run 
```
