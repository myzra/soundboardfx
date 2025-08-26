package com.maven.soundboard.soundboardfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX Soundboard Application
 * A modern desktop soundboard for Windows 11 with keybind support
 */
public class App extends Application {
    
    private SoundBoardController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/soundboard.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        
        // Create scene with modern styling
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Setup keyboard handling
        controller.setupKeyboardHandling(scene);
        
        // Configure stage
        primaryStage.setTitle("SoundBoard FX - Modern Desktop Soundboard");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        
        // Set application icon (optional - you can add an icon later)
        // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        
        // Handle application close
        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdown();
            }
        });
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}