package com.maven.soundboard.soundboardfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;

public class SoundBoardController implements Initializable {
    
    @FXML private TableView<SoundItem> soundTable;
    @FXML private TableColumn<SoundItem, String> nameColumn;
    @FXML private TableColumn<SoundItem, String> keybindColumn;
    @FXML private Button addSoundButton;
    @FXML private Button editSoundButton;
    @FXML private Button removeSoundButton;
    @FXML private Button stopAllButton;
    @FXML private Label statusLabel;
    
    private ObservableList<SoundItem> soundItems = FXCollections.observableArrayList();
    private SoundManager soundManager = new SoundManager();
    private ConfigManager configManager = new ConfigManager();
    private Map<String, SoundItem> keybindMap = new HashMap<>();
    private Set<String> pressedKeys = new HashSet<>();
    
    // Global hotkey support
    private GlobalHotkeyManager globalHotkeyManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadSounds();
        setupEventHandlers();
        
        // Initialize global hotkey manager
        globalHotkeyManager = new GlobalHotkeyManager(soundManager, () -> 
            updateStatus("Playing sound via global hotkey"));
        globalHotkeyManager.initialize();
        globalHotkeyManager.updateKeybinds(keybindMap);
        
        updateStatus("Ready - Global hotkeys active!");
    }
    
    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        keybindColumn.setCellValueFactory(new PropertyValueFactory<>("keybind"));
        
        soundTable.setItems(soundItems);
        soundTable.setRowFactory(tv -> {
            TableRow<SoundItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    playSelectedSound();
                }
            });
            return row;
        });
        
        // Enable selection
        soundTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    private void setupEventHandlers() {
        addSoundButton.setOnAction(e -> showAddSoundDialog());
        editSoundButton.setOnAction(e -> editSelectedSound());
        removeSoundButton.setOnAction(e -> removeSelectedSound());
        stopAllButton.setOnAction(e -> stopAllSounds());
        
        // Update button states based on selection
        soundTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editSoundButton.setDisable(!hasSelection);
            removeSoundButton.setDisable(!hasSelection);
        });
        
        // Initially disable edit/remove buttons
        editSoundButton.setDisable(true);
        removeSoundButton.setDisable(true);
    }
    
    public void setupKeyboardHandling(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
    }
    
    private void handleKeyPressed(KeyEvent event) {
        String keyString = getKeyString(event);
        if (keyString != null && !pressedKeys.contains(keyString)) {
            pressedKeys.add(keyString);
            String combination = String.join("+", pressedKeys.stream().sorted().toArray(String[]::new));
            
            SoundItem soundItem = keybindMap.get(combination);
            if (soundItem != null) {
                soundManager.playSound(soundItem);
                updateStatus("Playing: " + soundItem.getName());
                event.consume();
            }
        }
    }
    
    private void handleKeyReleased(KeyEvent event) {
        String keyString = getKeyString(event);
        if (keyString != null) {
            pressedKeys.remove(keyString);
        }
    }
    
    private String getKeyString(KeyEvent event) {
        KeyCode code = event.getCode();
        List<String> modifiers = new ArrayList<>();
        
        if (event.isControlDown()) modifiers.add("CTRL");
        if (event.isAltDown()) modifiers.add("ALT");
        if (event.isShiftDown()) modifiers.add("SHIFT");
        
        if (code != KeyCode.CONTROL && code != KeyCode.ALT && code != KeyCode.SHIFT) {
            modifiers.add(code.toString());
        }
        
        return modifiers.isEmpty() ? null : String.join("+", modifiers);
    }
    
    @FXML
    private void playSelectedSound() {
        SoundItem selected = soundTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            soundManager.playSound(selected);
            updateStatus("Playing: " + selected.getName());
        }
    }
    
    @FXML
    private void showAddSoundDialog() {
        showSoundDialog(null);
    }
    
    @FXML
    private void editSelectedSound() {
        SoundItem selected = soundTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSoundDialog(selected);
        }
    }
    
    @FXML
    private void removeSelectedSound() {
        SoundItem selected = soundTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Sound");
            alert.setHeaderText("Remove Sound Item");
            alert.setContentText("Are you sure you want to remove \"" + selected.getName() + "\"?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                soundItems.remove(selected);
                rebuildKeybindMap();
                saveSounds();
                updateStatus("Removed: " + selected.getName());
            }
        }
    }
    
    @FXML
    private void stopAllSounds() {
        soundManager.stopAllSounds();
        updateStatus("All sounds stopped");
    }
    
    private void showSoundDialog(SoundItem editItem) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editItem == null ? "Add Sound" : "Edit Sound");
        dialog.setResizable(false);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        // Name field
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        if (editItem != null) nameField.setText(editItem.getName());
        nameField.setPrefWidth(300);
        
        // File selection
        Label fileLabel = new Label("Sound File:");
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        TextField fileField = new TextField();
        fileField.setEditable(false);
        fileField.setPrefWidth(200);
        if (editItem != null) fileField.setText(editItem.getFilePath());
        
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Sound File");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.aac"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                fileField.setText(selectedFile.getAbsolutePath());
                if (nameField.getText().isEmpty()) {
                    String fileName = selectedFile.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    nameField.setText(dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName);
                }
            }
        });
        
        fileBox.getChildren().addAll(fileField, browseButton);
        
        // Keybind field
        Label keybindLabel = new Label("Keybind:");
        TextField keybindField = new TextField();
        if (editItem != null) keybindField.setText(editItem.getKeybind());
        keybindField.setEditable(false);
        keybindField.setPrefWidth(300);
        keybindField.setPromptText("Click and press key combination...");
        
        Set<String> capturedKeys = new HashSet<>();
        keybindField.setOnKeyPressed(event -> {
            String keyString = getKeyString(event);
            if (keyString != null) {
                capturedKeys.clear();
                capturedKeys.addAll(Arrays.asList(keyString.split("\\+")));
                keybindField.setText(keyString);
                event.consume();
            }
        });
        
        keybindField.setOnKeyReleased(event -> event.consume());
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button okButton = new Button(editItem == null ? "Add" : "Update");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String filePath = fileField.getText().trim();
            String keybind = keybindField.getText().trim();
            
            if (name.isEmpty() || filePath.isEmpty() || keybind.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Missing Information");
                alert.setContentText("Please fill in all fields.");
                alert.showAndWait();
                return;
            }
            
            // Check if file exists
            if (!new File(filePath).exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Error");
                alert.setHeaderText("File Not Found");
                alert.setContentText("The selected sound file could not be found.");
                alert.showAndWait();
                return;
            }
            
            // Check for keybind conflicts (excluding current item if editing)
            boolean hasConflict = soundItems.stream()
                .anyMatch(item -> !item.equals(editItem) && keybind.equals(item.getKeybind()));
            
            if (hasConflict) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Keybind Conflict");
                alert.setHeaderText("Duplicate Keybind");
                alert.setContentText("This keybind is already in use by another sound.");
                alert.showAndWait();
                return;
            }
            
            if (editItem == null) {
                // Add new item
                SoundItem newItem = new SoundItem(name, filePath, keybind);
                soundItems.add(newItem);
                updateStatus("Added: " + name);
            } else {
                // Update existing item
                editItem.setName(name);
                editItem.setFilePath(filePath);
                editItem.setKeybind(keybind);
                soundTable.refresh();
                updateStatus("Updated: " + name);
            }
            
            rebuildKeybindMap();
            saveSounds();
            dialog.close();
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(okButton, cancelButton);
        
        root.getChildren().addAll(
            nameLabel, nameField,
            fileLabel, fileBox,
            keybindLabel, keybindField,
            buttonBox
        );
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void rebuildKeybindMap() {
        keybindMap.clear();
        for (SoundItem item : soundItems) {
            keybindMap.put(item.getKeybind(), item);
        }
        
        // Update global hotkey manager with new keybinds
        if (globalHotkeyManager != null) {
            globalHotkeyManager.updateKeybinds(keybindMap);
        }
    }
    
    private void loadSounds() {
        List<SoundItem> loadedSounds = configManager.loadSounds();
        soundItems.clear();
        soundItems.addAll(loadedSounds);
        rebuildKeybindMap();
        updateStatus("Loaded " + loadedSounds.size() + " sounds");
    }
    
    private void saveSounds() {
        configManager.saveSounds(new ArrayList<>(soundItems));
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        
        // Auto-clear status after 3 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (statusLabel.getText().equals(message)) {
                statusLabel.setText("Ready - Global hotkeys active!");
            }
        }));
        timeline.play();
    }
    
    public void shutdown() {
        if (globalHotkeyManager != null) {
            globalHotkeyManager.shutdown();
        }
        soundManager.dispose();
    }
}