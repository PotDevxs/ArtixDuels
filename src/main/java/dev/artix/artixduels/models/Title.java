package dev.artix.artixduels.models;

import java.util.List;

public class Title {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private TitleRarity rarity;
    private List<String> requirements;
    private String badgeIcon;
    private boolean unlocked;
    public enum TitleRarity {
        COMMON("&7Comum"),
        RARE("&bRaro"),
        EPIC("&5Épico"),
        LEGENDARY("&6Lendário");

        private String displayName;
        TitleRarity(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    public Title(String id, String name, String displayName, String description, TitleRarity rarity) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.unlocked = false;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public TitleRarity getRarity() {
        return rarity;
    }
    public void setRarity(TitleRarity rarity) {
        this.rarity = rarity;
    }
    public List<String> getRequirements() {
        return requirements;
    }
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
    public String getBadgeIcon() {
        return badgeIcon;
    }
    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }
    public boolean isUnlocked() {
        return unlocked;
    }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
