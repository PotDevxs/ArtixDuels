package dev.artix.artixduels.models;

import java.util.HashMap;
import java.util.Map;

public class Theme {
    private String name;
    private String displayName;
    private String description;
    private Map<String, String> colors;
    private boolean seasonal;
    private String season;
    public Theme(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.colors = new HashMap<>();
        this.seasonal = false;
        this.season = null;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Map<String, String> getColors() {
        return colors;
    }
    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }
    public void setColor(String key, String color) {
        colors.put(key, color);
    }
    public String getColor(String key) {
        return colors.getOrDefault(key, "&f");
    }
    public boolean isSeasonal() {
        return seasonal;
    }
    public void setSeasonal(boolean seasonal) {
        this.seasonal = seasonal;
    }
    public String getSeason() {
        return season;
    }
    public void setSeason(String season) {
        this.season = season;
    }
}
