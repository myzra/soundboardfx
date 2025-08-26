package com.maven.soundboard.soundboardfx;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles global system-wide hotkeys using JNativeHook
 */
public class GlobalHotkeyManager implements NativeKeyListener {
    
    private final Map<String, SoundItem> keybindMap = new HashMap<>();
    private final Set<String> pressedKeys = new HashSet<>();
    private final SoundManager soundManager;
    private final Runnable statusUpdater;
    
    public GlobalHotkeyManager(SoundManager soundManager, Runnable statusUpdater) {
        this.soundManager = soundManager;
        this.statusUpdater = statusUpdater;
        
        // Disable JNativeHook logging to reduce console spam
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
    }
    
    /**
     * Initialize global hook
     */
    public void initialize() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Cleanup and unregister global hook
     */
    public void shutdown() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem unregistering the native hook.");
            System.err.println(ex.getMessage());
        }
    }
    
    /**
     * Update the keybind mappings
     */
    public void updateKeybinds(Map<String, SoundItem> newKeybindMap) {
        keybindMap.clear();
        keybindMap.putAll(newKeybindMap);
        
        // Debug: Print all keybinds
        System.out.println("Updated keybinds:");
        for (String key : newKeybindMap.keySet()) {
            System.out.println("  " + key + " -> " + newKeybindMap.get(key).getName());
        }
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyName = convertNativeKeyToJavaFX(e);
        if (keyName != null && !pressedKeys.contains(keyName)) {
            pressedKeys.add(keyName);
            String combination = buildKeyCombination();
            
            System.out.println("Key pressed: " + keyName + ", Combination: " + combination);
            
            SoundItem soundItem = keybindMap.get(combination);
            if (soundItem != null) {
                System.out.println("Playing sound: " + soundItem.getName());
                Platform.runLater(() -> {
                    soundManager.playSound(soundItem);
                    if (statusUpdater != null) {
                        statusUpdater.run();
                    }
                });
            }
        }
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        String keyName = convertNativeKeyToJavaFX(e);
        if (keyName != null) {
            pressedKeys.remove(keyName);
        }
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used
    }
    
    private String buildKeyCombination() {
        return String.join("+", pressedKeys.stream().sorted().toArray(String[]::new));
    }
    
    private String convertNativeKeyToJavaFX(NativeKeyEvent e) {
        // Use JNativeHook's built-in key text conversion and map to JavaFX equivalents
        String nativeKeyText = NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
        
        // Map JNativeHook key names to JavaFX key names
        switch (nativeKeyText) {
            // Modifiers
            case "ctrl":
            case "control": 
                return "CTRL";
            case "alt":
            case "alt graph":
                return "ALT";
            case "shift":
                return "SHIFT";
                
            // Letters (convert to uppercase)
            case "a": return "A";
            case "b": return "B";
            case "c": return "C";
            case "d": return "D";
            case "e": return "E";
            case "f": return "F";
            case "g": return "G";
            case "h": return "H";
            case "i": return "I";
            case "j": return "J";
            case "k": return "K";
            case "l": return "L";
            case "m": return "M";
            case "n": return "N";
            case "o": return "O";
            case "p": return "P";
            case "q": return "Q";
            case "r": return "R";
            case "s": return "S";
            case "t": return "T";
            case "u": return "U";
            case "v": return "V";
            case "w": return "W";
            case "x": return "X";
            case "y": return "Y";
            case "z": return "Z";
                
            // Numbers
            case "0": return "DIGIT0";
            case "1": return "DIGIT1";
            case "2": return "DIGIT2";
            case "3": return "DIGIT3";
            case "4": return "DIGIT4";
            case "5": return "DIGIT5";
            case "6": return "DIGIT6";
            case "7": return "DIGIT7";
            case "8": return "DIGIT8";
            case "9": return "DIGIT9";
                
            // Function keys
            case "f1": return "F1";
            case "f2": return "F2";
            case "f3": return "F3";
            case "f4": return "F4";
            case "f5": return "F5";
            case "f6": return "F6";
            case "f7": return "F7";
            case "f8": return "F8";
            case "f9": return "F9";
            case "f10": return "F10";
            case "f11": return "F11";
            case "f12": return "F12";
                
            // Special keys
            case "space": return "SPACE";
            case "enter": return "ENTER";
            case "tab": return "TAB";
            case "escape": return "ESCAPE";
            case "backspace": return "BACK_SPACE";
            case "delete": return "DELETE";
                
            // Arrow keys
            case "up": return "UP";
            case "down": return "DOWN";
            case "left": return "LEFT";
            case "right": return "RIGHT";
                
            default:
                // Debug: Print unknown keys
                System.out.println("Unknown key: '" + nativeKeyText + "' (code: " + e.getKeyCode() + ")");
                return null;
        }
    }
}