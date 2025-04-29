package observer;

import javafx.application.Platform;
import model.Task;
import util.DateUtils;
import util.Toast;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDateTime;

/**
 * DeadlineNotifier observes tasks and notifies when deadlines are approaching.
 * Implements the Observer in the Observer pattern.
 */
public class DeadlineNotifier implements TaskObserver {
    
    /**
     * Check if a task deadline is within 24 hours.
     * @param task The task to check
     * @return true if the deadline is within 24 hours, false otherwise
     */
    private boolean isDeadlineNear(Task task) {
        LocalDateTime deadline = task.getDeadline();
        if (deadline == null) return false;
        
        return DateUtils.isWithin24Hours(deadline);
    }
    
    @Override
    public void update(Task task, String event) {
        if ("DEADLINE_CHECK".equals(event) && isDeadlineNear(task)) {
            Platform.runLater(() -> {
                String message = String.format("⚠️ Deadline near: %s — %s", 
                        task.getName(), 
                        DateUtils.formatDateTime(task.getDeadline()));
                
                // Show both a toast notification and an alert
                Toast.show(message, 5000);
                
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Deadline Warning");
                alert.setHeaderText("Task Deadline Approaching");
                alert.setContentText(message);
                alert.show();
            });
        }
    }
}
