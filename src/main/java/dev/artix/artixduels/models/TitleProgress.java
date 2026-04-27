package dev.artix.artixduels.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TitleProgress {
    private UUID playerId;
    private String titleId;
    private Map<String, Integer> requirementProgress;
    private boolean unlocked;
    private long unlockedAt;
    public TitleProgress(UUID playerId, String titleId) {
        this.playerId = playerId;
        this.titleId = titleId;
        this.requirementProgress = new HashMap<>();
        this.unlocked = false;
        this.unlockedAt = 0;
    }
    public UUID getPlayerId() {
        return playerId;
    }
    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
    public String getTitleId() {
        return titleId;
    }
    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }
    public Map<String, Integer> getRequirementProgress() {
        return requirementProgress;
    }
    public void setRequirementProgress(Map<String, Integer> requirementProgress) {
        this.requirementProgress = requirementProgress;
    }
    public void addRequirementProgress(String requirement, int amount) {
        requirementProgress.put(requirement, requirementProgress.getOrDefault(requirement, 0) + amount);
    }
    public int getRequirementProgress(String requirement) {
        return requirementProgress.getOrDefault(requirement, 0);
    }
    public boolean isUnlocked() {
        return unlocked;
    }
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked && unlockedAt == 0) {
            this.unlockedAt = System.currentTimeMillis();
        }
    }
    public long getUnlockedAt() {
        return unlockedAt;
    }
    public void setUnlockedAt(long unlockedAt) {
        this.unlockedAt = unlockedAt;
    }
}

