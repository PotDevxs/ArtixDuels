package dev.artix.artixduels.models;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Representa um passe de batalha.
 */
public class BattlePass {
    private String id;
    private String name;
    private long startTime;
    private long endTime;
    private int maxLevel;
    private Map<Integer, BattlePassReward> freeRewards;
    private Map<Integer, BattlePassReward> premiumRewards;
    private Map<UUID, BattlePassProgress> playerProgress;

    public BattlePass(String id, String name, long startTime, long endTime, int maxLevel) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxLevel = maxLevel;
        this.freeRewards = new java.util.HashMap<>();
        this.premiumRewards = new java.util.HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Map<Integer, BattlePassReward> getFreeRewards() {
        return freeRewards;
    }

    public void setFreeRewards(Map<Integer, BattlePassReward> freeRewards) {
        this.freeRewards = freeRewards;
    }

    public Map<Integer, BattlePassReward> getPremiumRewards() {
        return premiumRewards;
    }

    public void setPremiumRewards(Map<Integer, BattlePassReward> premiumRewards) {
        this.premiumRewards = premiumRewards;
    }

    public Map<UUID, BattlePassProgress> getPlayerProgress() {
        return playerProgress;
    }

    public void setPlayerProgress(Map<UUID, BattlePassProgress> playerProgress) {
        this.playerProgress = playerProgress;
    }

    public boolean isActive() {
        long now = System.currentTimeMillis();
        return now >= startTime && now <= endTime;
    }

    public static class BattlePassReward {
        private String type;
        private Map<String, Object> data;

        public BattlePassReward(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }

    public static class BattlePassProgress {
        private UUID playerId;
        private int level;
        private int xp;
        private boolean premium;
        private Map<Integer, Boolean> claimedRewards;

        public BattlePassProgress(UUID playerId) {
            this.playerId = playerId;
            this.level = 1;
            this.xp = 0;
            this.premium = false;
            this.claimedRewards = new java.util.HashMap<>();
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getXp() {
            return xp;
        }

        public void setXp(int xp) {
            this.xp = xp;
        }

        public void addXp(int amount) {
            this.xp += amount;
            updateLevel();
        }

        public boolean isPremium() {
            return premium;
        }

        public void setPremium(boolean premium) {
            this.premium = premium;
        }

        public Map<Integer, Boolean> getClaimedRewards() {
            return claimedRewards;
        }

        private void updateLevel() {
            int xpPerLevel = 100;
            int newLevel = (xp / xpPerLevel) + 1;
            if (newLevel > level) {
                level = Math.min(newLevel, 100);
            }
        }

        /** Recalcula nível a partir do XP (útil após carregar do arquivo). */
        public void syncLevelFromXp(int maxLevelCap) {
            int xpPerLevel = 100;
            int calculated = Math.max(1, (xp / xpPerLevel) + 1);
            this.level = Math.min(calculated, Math.max(1, maxLevelCap));
        }
    }
}

