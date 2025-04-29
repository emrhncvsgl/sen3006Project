package observer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Task;
import model.TaskStatus;

/**
 * TaskCompletionNotifier observes tasks and notifies when they are completed.
 * Implements the Observer in the Observer pattern.
 */
public class TaskCompletionNotifier implements TaskObserver {
    
    @Override
    public void update(Task task, String event) {
        if ("COMPLETED".equals(event) && task.getStatus() == TaskStatus.COMPLETED) {
            Platform.runLater(() -> {
                String message = String.format("✅ Completed: %s", task.getName());
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Task Completed");
                alert.setHeaderText("Task Marked as Complete");
                alert.setContentText(message);
                alert.show();
            });
        }
    }
}
