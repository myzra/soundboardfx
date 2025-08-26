package com.maven.soundboard.soundboardfx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages saving and loading sound configurations to/from JSON
 */
public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".soundboardfx";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "sounds.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ConfigManager() {
        // Create config directory if it doesn't exist
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }
    
    /**
     * Saves the sound items to the configuration file
     * @param soundItems List of sound items to save
     */
    public void saveSounds(List<SoundItem> soundItems) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), soundItems);
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }
    
    /**
     * Loads sound items from the configuration file
     * @return List of sound items, empty if file doesn't exist or error occurs
     */
    public List<SoundItem> loadSounds() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return new ArrayList<>();
        }
        
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, SoundItem.class);
            return objectMapper.readValue(configFile, listType);
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
