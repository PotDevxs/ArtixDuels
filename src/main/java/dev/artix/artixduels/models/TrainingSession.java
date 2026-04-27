package dev.artix.artixduels.models;

import java.util.UUID;

public class TrainingSession {
    private UUID sessionId;
    private UUID playerId;
    private TrainingBot bot;
    private String kitName;
    private String arenaName;
    private DuelMode mode;
    private long startTime;
    private long endTime;
    private TrainingStats stats;
    private boolean active;

    public TrainingSession(UUID playerId, TrainingBot bot, String kitName, 
                          String arenaName, DuelMode mode) {
        this.sessionId = UUID.randomUUID();
        this.playerId = playerId;
        this.bot = bot;
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();
        this.stats = new TrainingStats();
        this.active = true;
    }
    public UUID getSessionId() {
        return sessionId;
    }
    public UUID getPlayerId() {
        return playerId;
    }
    public TrainingBot getBot() {
        return bot;
    }
    public String getKitName() {
        return kitName;
    }
    public String getArenaName() {
        return arenaName;
    }
    public DuelMode getMode() {
        return mode;
    }
    public long getStartTime() {
        return startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public long getDuration() {
        if (endTime == 0) {
            return System.currentTimeMillis() - startTime;
        }
        return endTime - startTime;
    }
    public TrainingStats getStats() {
        return stats;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.endTime = System.currentTimeMillis();
        }
    }
    public static class TrainingStats {
        private int playerHits;
        private int botHits;
        private int playerKills;
        private int botKills;
        private int playerCombos;
        private int botCombos;
        private double playerDamageDealt;
        private double botDamageDealt;
        private double playerDamageTaken;
        private double botDamageTaken;
        public TrainingStats() {
            this.playerHits = 0;
            this.botHits = 0;
            this.playerKills = 0;
            this.botKills = 0;
            this.playerCombos = 0;
            this.botCombos = 0;
            this.playerDamageDealt = 0;
            this.botDamageDealt = 0;
            this.playerDamageTaken = 0;
            this.botDamageTaken = 0;
        }
        public int getPlayerHits() {
            return playerHits;
        }
        public void addPlayerHit() {
            this.playerHits++;
        }
        public int getBotHits() {
            return botHits;
        }
        public void addBotHit() {
            this.botHits++;
        }
        public int getPlayerKills() {
            return playerKills;
        }
        public void addPlayerKill() {
            this.playerKills++;
        }
        public int getBotKills() {
            return botKills;
        }
        public void addBotKill() {
            this.botKills++;
        }
        public int getPlayerCombos() {
            return playerCombos;
        }
        public void addPlayerCombo() {
            this.playerCombos++;
        }
        public int getBotCombos() {
            return botCombos;
        }
        public void addBotCombo() {
            this.botCombos++;
        }
        public double getPlayerDamageDealt() {
            return playerDamageDealt;
        }
        public void addPlayerDamageDealt(double damage) {
            this.playerDamageDealt += damage;
        }
        public double getBotDamageDealt() {
            return botDamageDealt;
        }
        public void addBotDamageDealt(double damage) {
            this.botDamageDealt += damage;
        }
        public double getPlayerDamageTaken() {
            return playerDamageTaken;
        }
        public void addPlayerDamageTaken(double damage) {
            this.playerDamageTaken += damage;
        }
        public double getBotDamageTaken() {
            return botDamageTaken;
        }
        public void addBotDamageTaken(double damage) {
            this.botDamageTaken += damage;
        }
        public double getPlayerAccuracy() {
            if (playerHits + botHits == 0) return 0.0;
            return (double) playerHits / (playerHits + botHits) * 100.0;
        }
        public double getPlayerWinRate() {
            int totalRounds = playerKills + botKills;
            if (totalRounds == 0) return 0.0;
            return (double) playerKills / totalRounds * 100.0;
        }
    }
}
