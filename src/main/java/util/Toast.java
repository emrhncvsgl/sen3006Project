package util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Utility class for displaying toast-style notifications.
 */
public class Toast {
    public static final Color TOAST_BACKGROUND_COLOR = Color.rgb(30, 30, 30, 0.9);
    public static final Color TOAST_TEXT_COLOR = Color.WHITE;
    public static final int TOAST_PADDING = 10;
    public static final int TOAST_MIN_WIDTH = 250;
    public static final int TOAST_HEIGHT = 50;

    /**
     * Show a toast notification for a specified duration.
     * @param message The message to display
     * @param durationMillis The duration to display the toast (in milliseconds)
     */
    public static void show(String message, int durationMillis) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            
            Label toastLabel = new Label(message);
            toastLabel.setTextFill(TOAST_TEXT_COLOR);
            
            StackPane root = new StackPane(toastLabel);
            root.setStyle(
                "-fx-background-radius: 5; " +
                "-fx-background-color: rgba(30, 30, 30, 0.9); " +
                "-fx-padding: " + TOAST_PADDING + "px;"
            );
            
            root.setMinWidth(TOAST_MIN_WIDTH);
            root.setMinHeight(TOAST_HEIGHT);
            root.setAlignment(Pos.CENTER);
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);
            
            // Get primary stage to position the toast
            Stage primaryStage = findPrimaryStage();
            if (primaryStage != null) {
                double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
                double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;
                
                toastStage.setX(centerX - TOAST_MIN_WIDTH / 2);
                toastStage.setY(centerY - TOAST_HEIGHT / 2);
            }
            
            toastStage.show();
            
            // Fade-in animation
            Timeline fadeInTimeline = new Timeline();
            KeyFrame fadeInKey = new KeyFrame(Duration.millis(200),
                    new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
            fadeInTimeline.getKeyFrames().add(fadeInKey);
            fadeInTimeline.play();
            
            // Wait for specified duration
            Timeline delayTimeline = new Timeline();
            delayTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(durationMillis), ae -> {}));
            delayTimeline.play();
            
            // Fade-out animation
            delayTimeline.setOnFinished((ae) -> {
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey = new KeyFrame(Duration.millis(500),
                        new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey);
                fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
                fadeOutTimeline.play();
            });
        });
    }
    
    /**
     * Find the primary stage to position the toast notification.
     * @return The primary stage of the application
     */
    private static Stage findPrimaryStage() {
        for (Stage stage : StageHelper.getStages()) {
            if (stage.isShowing()) {
                return stage;
            }
        }
        return null;
    }
    
    /**
     * Helper class to access all stages.
     */
    private static class StageHelper {
        /**
         * Get all active stages.
         * @return Stage array with all active stages
         */
        public static Stage[] getStages() {
            return Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage)
                    .map(window -> (Stage) window)
                    .toArray(Stage[]::new);
        }
    }
}
