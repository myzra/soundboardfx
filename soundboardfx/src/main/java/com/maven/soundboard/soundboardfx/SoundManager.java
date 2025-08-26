package com.maven.soundboard.soundboardfx;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound playback with MediaPlayer instances
 */
public class SoundManager {
    private final Map<String, MediaPlayer> mediaPlayers = new HashMap<>();
    
    /**
     * Plays a sound file
     * @param soundItem The sound item to play
     */
    public void playSound(SoundItem soundItem) {
        try {
            File soundFile = new File(soundItem.getFilePath());
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundItem.getFilePath());
                return;
            }

            // Stop any currently playing instance of this sound
            stopSound(soundItem.getFilePath());

            Media media = new Media(soundFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                mediaPlayers.remove(soundItem.getFilePath());
            });
            
            mediaPlayer.setOnError(() -> {
                System.err.println("Error playing sound: " + mediaPlayer.getError().getMessage());
                mediaPlayer.dispose();
                mediaPlayers.remove(soundItem.getFilePath());
            });
            
            mediaPlayers.put(soundItem.getFilePath(), mediaPlayer);
            mediaPlayer.play();
            
        } catch (Exception e) {
            System.err.println("Failed to play sound: " + e.getMessage());
        }
    }
    
    /**
     * Stops a currently playing sound
     * @param filePath The file path of the sound to stop
     */
    public void stopSound(String filePath) {
        MediaPlayer player = mediaPlayers.get(filePath);
        if (player != null) {
            player.stop();
            player.dispose();
            mediaPlayers.remove(filePath);
        }
    }
    
    /**
     * Stops all currently playing sounds
     */
    public void stopAllSounds() {
        mediaPlayers.values().forEach(player -> {
            player.stop();
            player.dispose();
        });
        mediaPlayers.clear();
    }
    
    /**
     * Cleanup resources
     */
    public void dispose() {
        stopAllSounds();
    }
}