package com.maven.soundboard.soundboardfx;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a sound item with its file path, display name, and keybind
 */
public class SoundItem {
	private String name;
	private String filePath;
	private String keybind;
	
	@JsonCreator
	public SoundItem(@JsonProperty("name") String name,
					@JsonProperty("filePath") String filePath,
					@JsonProperty("keybind") String keybind) {
		this.name = name;
		this.filePath = filePath;
		this.keybind = keybind;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getKeybind() {
		return keybind;
	}
	
	public void setKeybind(String keybind) {
		this.keybind = keybind;
	}
	
	@Override
	public String toString() {
		return name + " (" + keybind + ")";
	}
}
