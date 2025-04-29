package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import model.CompositeTask;
import model.SimpleTask;
import model.Task;
import model.TaskStatus;
import observer.DeadlineNotifier;
import observer.TaskCompletionNotifier;
import observer.TaskObserver;
import util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the main view of the task management application.
 * Connects the model (Task objects) with the view (FXML).
 */
public class MainController {
    @FXML
    private TreeView<Task> taskTreeView;

    @FXML
    private TextField taskNameField;

    @FXML
    private DatePicker deadlineDatePicker;

    @FXML
    private ComboBox<String> deadlineHourComboBox;

    @FXML
    private ComboBox<String> deadlineMinuteComboBox;

    @FXML
    private RadioButton notStartedRadio;

    @FXML
    private RadioButton inProgressRadio;

    @FXML
    private RadioButton completedRadio;

    @FXML
    private TableView<Task> subtasksTableView;

    @FXML
    private TableColumn<Task, String> subtaskNameColumn;

    @FXML
    private TableColumn<Task, String> subtaskStatusColumn;

    @FXML
    private TableColumn<Task, String> subtaskDeadlineColumn;

    @FXML
    private Label statusLabel;

    private ToggleGroup statusGroup;

    private final ObservableList<Task> rootTasks = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Observers for task events
    private final TaskObserver deadlineObserver = new DeadlineNotifier();
    private final TaskObserver completionObserver = new TaskCompletionNotifier();

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        setupTreeView();
        setupDetailForm();
        setupSubtasksTable();
        setupDeadlineChecker();
        
        // Create some example tasks for demonstration
        createSampleTasks();
    }

    /**
     * Set up the TreeView to display tasks.
     */
    private void setupTreeView() {
        TreeItem<Task> rootItem = new TreeItem<>();
        taskTreeView.setRoot(rootItem);
        taskTreeView.setShowRoot(false);
        
        // Handle tree selection changes
        taskTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayTaskDetails(newValue.getValue());
                updateSubtasksTable(newValue.getValue());
            } else {
                clearTaskDetails();
                subtasksTableView.getItems().clear();
            }
        });
        
        // Handle double-click to toggle edit mode
        taskTreeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Toggle edit mode...
                    taskNameField.requestFocus();
                }
            }
        });
        
        // Set up context menu to only show appropriate options based on task type
        taskTreeView.setOnContextMenuRequested(event -> {
            TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Task task = selectedItem.getValue();
                // Create a new context menu with appropriate options
                ContextMenu contextMenu = new ContextMenu();
                
                // Add different menu items based on task type
                if (task instanceof CompositeTask) {
                    // For CompositeTask, show all options
                    MenuItem addSimpleSubtask = new MenuItem("Add Simple Subtask");
                    addSimpleSubtask.setOnAction(e -> handleAddSimpleSubtask());
                    
                    MenuItem addCompositeSubtask = new MenuItem("Add Composite Subtask");
                    addCompositeSubtask.setOnAction(e -> handleAddCompositeSubtask());
                    
                    MenuItem markCompleted = new MenuItem("Mark Completed");
                    markCompleted.setOnAction(e -> handleMarkCompleted());
                    
                    MenuItem deleteTask = new MenuItem("Delete");
                    deleteTask.setOnAction(e -> handleDeleteTask());
                    
                    contextMenu.getItems().addAll(addSimpleSubtask, addCompositeSubtask, markCompleted, deleteTask);
                } else {
                    // For SimpleTask, only show options that don't involve adding subtasks
                    MenuItem markCompleted = new MenuItem("Mark Completed");
                    markCompleted.setOnAction(e -> handleMarkCompleted());
                    
                    MenuItem deleteTask = new MenuItem("Delete");
                    deleteTask.setOnAction(e -> handleDeleteTask());
                    
                    contextMenu.getItems().addAll(markCompleted, deleteTask);
                }
                
                // Show the context menu
                contextMenu.show(taskTreeView, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });
    }
    
    /**
     * Set up the form for editing task details.
     */
    private void setupDetailForm() {
        statusGroup = new ToggleGroup();
        notStartedRadio.setToggleGroup(statusGroup);
        inProgressRadio.setToggleGroup(statusGroup);
        completedRadio.setToggleGroup(statusGroup);
        
        // Populate hour combobox (00-23)
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        deadlineHourComboBox.setItems(FXCollections.observableArrayList(hours));
        
        // Populate minute combobox (00-59)
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        deadlineMinuteComboBox.setItems(FXCollections.observableArrayList(minutes));
        
        // Default values
        deadlineHourComboBox.setValue("12");
        deadlineMinuteComboBox.setValue("00");
    }
    
    /**
     * Set up the table view for displaying subtasks.
     */
    private void setupSubtasksTable() {
        subtaskNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        
        subtaskStatusColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStatus().toString()));
        subtaskStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    if (TaskStatus.COMPLETED.getDisplayName().equals(item)) {
                        setTextFill(Color.GREEN);
                    } else if (TaskStatus.IN_PROGRESS.getDisplayName().equals(item)) {
                        setTextFill(Color.BLUE);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        
        subtaskDeadlineColumn.setCellValueFactory(param -> 
            new SimpleStringProperty(DateUtils.formatDateTime(param.getValue().getDeadline())));
        subtaskDeadlineColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    Task task = getTableRow().getItem();
                    if (task != null && task.getDeadline() != null) {
                        if (DateUtils.isWithin24Hours(task.getDeadline())) {
                            setTextFill(Color.RED);
                        } else {
                            setTextFill(Color.BLACK);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Set up a scheduled task to periodically check task deadlines.
     */
    private void setupDeadlineChecker() {
        // Schedule a task to check deadlines every minute
        scheduler.scheduleAtFixedRate(() -> {
            checkAllDeadlines();
        }, 0, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Create sample tasks for demonstration.
     */
    private void createSampleTasks() {
        // Create a composite task with subtasks
        CompositeTask projectTask = new CompositeTask("Project Alpha");
        projectTask.setDeadline(LocalDateTime.now().plusDays(7));
        projectTask.attachObserver(deadlineObserver);
        projectTask.attachObserver(completionObserver);
        
        // Create subtasks
        SimpleTask designTask = new SimpleTask("Design Phase", LocalDateTime.now().plusDays(2));
        designTask.attachObserver(deadlineObserver);
        designTask.attachObserver(completionObserver);
        designTask.setStatus(TaskStatus.IN_PROGRESS);
        
        CompositeTask implementationTask = new CompositeTask("Implementation Phase", LocalDateTime.now().plusDays(5));
        implementationTask.attachObserver(deadlineObserver);
        implementationTask.attachObserver(completionObserver);
        
        SimpleTask testingTask = new SimpleTask("Testing Phase", LocalDateTime.now().plusDays(6));
        testingTask.attachObserver(deadlineObserver);
        testingTask.attachObserver(completionObserver);
        
        // Add frontend and backend subtasks to implementation task
        SimpleTask frontendTask = new SimpleTask("Frontend Implementation", LocalDateTime.now().plusDays(4));
        frontendTask.attachObserver(deadlineObserver);
        frontendTask.attachObserver(completionObserver);
        
        SimpleTask backendTask = new SimpleTask("Backend Implementation", LocalDateTime.now().plusDays(3));
        backendTask.attachObserver(deadlineObserver);
        backendTask.attachObserver(completionObserver);
        
        implementationTask.addSubtask(frontendTask);
        implementationTask.addSubtask(backendTask);
        
        // Build the hierarchy
        projectTask.addSubtask(designTask);
        projectTask.addSubtask(implementationTask);
        projectTask.addSubtask(testingTask);
        
        // Add another independent task with a near deadline for demonstration
        SimpleTask urgentTask = new SimpleTask("Urgent Task", LocalDateTime.now().plusHours(12));
        urgentTask.attachObserver(deadlineObserver);
        urgentTask.attachObserver(completionObserver);
        
        // Add to root tasks and update the tree
        rootTasks.add(projectTask);
        rootTasks.add(urgentTask);
        refreshTaskTree();
    }
    
    /**
     * Refresh the task tree view with current tasks.
     */
    private void refreshTaskTree() {
        TreeItem<Task> root = taskTreeView.getRoot();
        root.getChildren().clear();
        
        for (Task task : rootTasks) {
            TreeItem<Task> taskItem = createTreeItem(task);
            root.getChildren().add(taskItem);
        }
        
        // Expand the first level
        root.setExpanded(true);
        for (TreeItem<Task> item : root.getChildren()) {
            item.setExpanded(true);
        }
    }
    
    /**
     * Create a tree item for a task and its subtasks.
     * @param task The task to create a tree item for
     * @return The created tree item
     */
    private TreeItem<Task> createTreeItem(Task task) {
        TreeItem<Task> item = new TreeItem<>(task);
        
        for (Task child : task.getChildren()) {
            item.getChildren().add(createTreeItem(child));
        }
        
        return item;
    }
    
    /**
     * Display the details of the selected task in the form.
     * @param task The task to display
     */
    private void displayTaskDetails(Task task) {
        if (task == null) {
            clearTaskDetails();
            return;
        }
        
        taskNameField.setText(task.getName());
        
        LocalDateTime deadline = task.getDeadline();
        if (deadline != null) {
            deadlineDatePicker.setValue(deadline.toLocalDate());
            deadlineHourComboBox.setValue(String.format("%02d", deadline.getHour()));
            deadlineMinuteComboBox.setValue(String.format("%02d", deadline.getMinute()));
        } else {
            deadlineDatePicker.setValue(null);
            deadlineHourComboBox.setValue("12");
            deadlineMinuteComboBox.setValue("00");
        }
        
        switch (task.getStatus()) {
            case NOT_STARTED -> notStartedRadio.setSelected(true);
            case IN_PROGRESS -> inProgressRadio.setSelected(true);
            case COMPLETED -> completedRadio.setSelected(true);
        }
    }
    
    /**
     * Clear the task details form.
     */
    private void clearTaskDetails() {
        taskNameField.clear();
        deadlineDatePicker.setValue(null);
        deadlineHourComboBox.setValue("12");
        deadlineMinuteComboBox.setValue("00");
        notStartedRadio.setSelected(true);
    }
    
    /**
     * Update the subtasks table with the children of the selected task.
     * @param task The task whose subtasks should be displayed
     */
    private void updateSubtasksTable(Task task) {
        if (task != null) {
            subtasksTableView.getItems().clear();
            subtasksTableView.getItems().addAll(task.getChildren());
        }
    }
    
    /**
     * Handler for adding a simple task.
     */
    @FXML
    private void handleAddSimpleTask() {
        TextInputDialog dialog = new TextInputDialog("New Task");
        dialog.setTitle("Add Simple Task");
        dialog.setHeaderText("Enter the name for the new task:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            SimpleTask newTask = new SimpleTask(name);
            newTask.attachObserver(deadlineObserver);
            newTask.attachObserver(completionObserver);
            rootTasks.add(newTask);
            refreshTaskTree();
            statusLabel.setText("Simple task added: " + name);
        });
    }
    
    /**
     * Handler for adding a composite task.
     */
    @FXML
    private void handleAddCompositeTask() {
        TextInputDialog dialog = new TextInputDialog("New Composite Task");
        dialog.setTitle("Add Composite Task");
        dialog.setHeaderText("Enter the name for the new composite task:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            CompositeTask newTask = new CompositeTask(name);
            newTask.attachObserver(deadlineObserver);
            newTask.attachObserver(completionObserver);
            rootTasks.add(newTask);
            refreshTaskTree();
            statusLabel.setText("Composite task added: " + name);
        });
    }
    
    /**
     * Handler for adding a simple subtask to the selected task.
     */
    @FXML
    private void handleAddSimpleSubtask() {
        TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task first.");
            return;
        }
        
        Task selectedTask = selectedItem.getValue();
        
        TextInputDialog dialog = new TextInputDialog("New Subtask");
        dialog.setTitle("Add Simple Subtask");
        dialog.setHeaderText("Enter the name for the new subtask:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                SimpleTask newSubtask = new SimpleTask(name);
                newSubtask.attachObserver(deadlineObserver);
                newSubtask.attachObserver(completionObserver);
                selectedTask.addSubtask(newSubtask);
                refreshTaskTree();
                updateSubtasksTable(selectedTask);
                statusLabel.setText("Simple subtask added: " + name);
            } catch (UnsupportedOperationException e) {
                showAlert("Operation Failed", "Cannot add subtasks to a SimpleTask.");
            }
        });
    }
    
    /**
     * Handler for adding a composite subtask to the selected task.
     */
    @FXML
    private void handleAddCompositeSubtask() {
        TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task first.");
            return;
        }
        
        Task selectedTask = selectedItem.getValue();
        
        TextInputDialog dialog = new TextInputDialog("New Composite Subtask");
        dialog.setTitle("Add Composite Subtask");
        dialog.setHeaderText("Enter the name for the new composite subtask:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                CompositeTask newSubtask = new CompositeTask(name);
                newSubtask.attachObserver(deadlineObserver);
                newSubtask.attachObserver(completionObserver);
                selectedTask.addSubtask(newSubtask);
                refreshTaskTree();
                updateSubtasksTable(selectedTask);
                statusLabel.setText("Composite subtask added: " + name);
            } catch (UnsupportedOperationException e) {
                showAlert("Operation Failed", "Cannot add subtasks to a SimpleTask.");
            }
        });
    }
    
    /**
     * Handler for marking a task as completed.
     */
    @FXML
    private void handleMarkCompleted() {
        TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task first.");
            return;
        }
        
        Task selectedTask = selectedItem.getValue();
        selectedTask.setStatus(TaskStatus.COMPLETED);
        
        refreshTaskTree();
        displayTaskDetails(selectedTask);
        statusLabel.setText("Task marked as completed: " + selectedTask.getName());
    }
    
    /**
     * Handler for deleting a task.
     */
    @FXML
    private void handleDeleteTask() {
        TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task first.");
            return;
        }
        
        TreeItem<Task> parentItem = selectedItem.getParent();
        Task selectedTask = selectedItem.getValue();
        
        if (parentItem == taskTreeView.getRoot()) {
            rootTasks.remove(selectedTask);
        } else {
            Task parentTask = parentItem.getValue();
            parentTask.removeSubtask(selectedTask);
            updateSubtasksTable(parentTask);
        }
        
        refreshTaskTree();
        statusLabel.setText("Task deleted: " + selectedTask.getName());
    }
    
    /**
     * Handler for applying changes to a task from the form.
     */
    @FXML
    private void handleApplyChanges() {
        TreeItem<Task> selectedItem = taskTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("No Selection", "Please select a task first.");
            return;
        }
        
        Task selectedTask = selectedItem.getValue();
        
        // Update task properties
        selectedTask.setName(taskNameField.getText());
        
        // Update deadline if date is selected
        LocalDate selectedDate = deadlineDatePicker.getValue();
        if (selectedDate != null) {
            int hour = Integer.parseInt(deadlineHourComboBox.getValue());
            int minute = Integer.parseInt(deadlineMinuteComboBox.getValue());
            LocalDateTime deadline = LocalDateTime.of(selectedDate, LocalTime.of(hour, minute));
            selectedTask.setDeadline(deadline);
        } else {
            selectedTask.setDeadline(null);
        }
        
        // Update status based on radio button selection
        if (notStartedRadio.isSelected()) {
            selectedTask.setStatus(TaskStatus.NOT_STARTED);
        } else if (inProgressRadio.isSelected()) {
            selectedTask.setStatus(TaskStatus.IN_PROGRESS);
        } else if (completedRadio.isSelected()) {
            selectedTask.setStatus(TaskStatus.COMPLETED);
        }
        
        refreshTaskTree();
        updateSubtasksTable(selectedTask);
        statusLabel.setText("Changes applied to: " + selectedTask.getName());
    }
    
    /**
     * Handler for saving tasks (stub for future persistence implementation).
     */
    @FXML
    private void handleSave() {
        // This would save the tasks to persistent storage
        statusLabel.setText("Tasks saved (demonstration only - not actually saved)");
        showAlert("Save", "Tasks would be saved here in a future implementation.");
    }
    
    /**
     * Handler for exiting the application.
     */
    @FXML
    private void handleExit() {
        // Clean up resources
        scheduler.shutdown();
        System.exit(0);
    }
    
    /**
     * Check all task deadlines and notify if necessary.
     */
    private void checkAllDeadlines() {
        // Recursively check all tasks in the hierarchy
        for (Task task : rootTasks) {
            checkTaskDeadline(task);
        }
    }
    
    /**
     * Check a specific task's deadline and notify if necessary.
     * @param task The task to check
     */
    private void checkTaskDeadline(Task task) {
        // Check this task's deadline
        task.notifyObservers("DEADLINE_CHECK");
        
        // Check all child tasks recursively
        for (Task child : task.getChildren()) {
            checkTaskDeadline(child);
        }
    }
    
    /**
     * Display an alert dialog.
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
