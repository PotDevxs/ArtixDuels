package dev.artix.artixduels.models;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class Duel {
    private UUID player1Id;
    private UUID player2Id;
    private String kitName;
    private String arenaName;
    private DuelMode mode;
    private DuelState state;
    private long startTime;
    private Set<UUID> spectators;
    
    // Estatísticas do duelo por modo
    private int player1Kills;
    private int player2Kills;
    private int player1Goals;
    private int player2Goals;
    private int player1Lives;
    private int player2Lives;
    private int player1Hits;
    private int player2Hits;
    private int player1Combo;
    private int player2Combo;
    private boolean player1BedAlive;
    private boolean player2BedAlive;
    private int player1Sopas;
    private int player2Sopas;
    private int player1Streak;
    private int player2Streak;

    public enum DuelState {
        COUNTDOWN,
        FIGHTING,
        ENDING
    }

    public Duel(Player player1, Player player2, String kitName, String arenaName, DuelMode mode) {
        this.player1Id = player1.getUniqueId();
        this.player2Id = player2.getUniqueId();
        this.kitName = kitName;
        this.arenaName = arenaName;
        this.mode = mode;
        this.state = DuelState.COUNTDOWN;
        this.startTime = System.currentTimeMillis();
        this.spectators = new HashSet<>();
        
        // Inicializar estatísticas baseadas no modo
        initializeModeStats(mode);
    }
    
    private void initializeModeStats(DuelMode mode) {
        this.player1Kills = 0;
        this.player2Kills = 0;
        this.player1Goals = 0;
        this.player2Goals = 0;
        this.player1Hits = 0;
        this.player2Hits = 0;
        this.player1Combo = 0;
        this.player2Combo = 0;
        this.player1Sopas = 0;
        this.player2Sopas = 0;
        this.player1Streak = 0;
        this.player2Streak = 0;
        
        switch (mode) {
            case BEDFIGHT:
                this.player1BedAlive = true;
                this.player2BedAlive = true;
                break;
            case STICKFIGHT:
                this.player1Lives = 3;
                this.player2Lives = 3;
                break;
            default:
                this.player1BedAlive = true;
                this.player2BedAlive = true;
                this.player1Lives = 0;
                this.player2Lives = 0;
                break;
        }
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public void setArenaName(String arenaName) {
        this.arenaName = arenaName;
    }

    public DuelState getState() {
        return state;
    }

    public void setState(DuelState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Set<UUID> getSpectators() {
        return spectators;
    }

    public void setSpectators(Set<UUID> spectators) {
        this.spectators = spectators;
    }

    public boolean isPlayer(UUID playerId) {
        return player1Id.equals(playerId) || player2Id.equals(playerId);
    }

    public UUID getOpponent(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player2Id;
        } else if (player2Id.equals(playerId)) {
            return player1Id;
        }
        return null;
    }

    public DuelMode getMode() {
        return mode;
    }

    public void setMode(DuelMode mode) {
        this.mode = mode;
    }
    
    // Getters e Setters para estatísticas
    public int getPlayer1Kills() {
        return player1Kills;
    }
    
    public void setPlayer1Kills(int player1Kills) {
        this.player1Kills = player1Kills;
    }
    
    public void addPlayer1Kill() {
        this.player1Kills++;
    }
    
    public int getPlayer2Kills() {
        return player2Kills;
    }
    
    public void setPlayer2Kills(int player2Kills) {
        this.player2Kills = player2Kills;
    }
    
    public void addPlayer2Kill() {
        this.player2Kills++;
    }
    
    public int getPlayer1Goals() {
        return player1Goals;
    }
    
    public void setPlayer1Goals(int player1Goals) {
        this.player1Goals = player1Goals;
    }
    
    public void addPlayer1Goal() {
        this.player1Goals++;
    }
    
    public int getPlayer2Goals() {
        return player2Goals;
    }
    
    public void setPlayer2Goals(int player2Goals) {
        this.player2Goals = player2Goals;
    }
    
    public void addPlayer2Goal() {
        this.player2Goals++;
    }
    
    public int getPlayer1Lives() {
        return player1Lives;
    }
    
    public void setPlayer1Lives(int player1Lives) {
        this.player1Lives = player1Lives;
    }
    
    public void removePlayer1Life() {
        if (this.player1Lives > 0) {
            this.player1Lives--;
        }
    }
    
    public int getPlayer2Lives() {
        return player2Lives;
    }
    
    public void setPlayer2Lives(int player2Lives) {
        this.player2Lives = player2Lives;
    }
    
    public void removePlayer2Life() {
        if (this.player2Lives > 0) {
            this.player2Lives--;
        }
    }
    
    public int getPlayer1Hits() {
        return player1Hits;
    }
    
    public void setPlayer1Hits(int player1Hits) {
        this.player1Hits = player1Hits;
    }
    
    public void addPlayer1Hit() {
        this.player1Hits++;
        this.player1Combo++;
    }
    
    public int getPlayer2Hits() {
        return player2Hits;
    }
    
    public void setPlayer2Hits(int player2Hits) {
        this.player2Hits = player2Hits;
    }
    
    public void addPlayer2Hit() {
        this.player2Hits++;
        this.player2Combo++;
    }
    
    public int getPlayer1Combo() {
        return player1Combo;
    }
    
    public void setPlayer1Combo(int player1Combo) {
        this.player1Combo = player1Combo;
    }
    
    public void resetPlayer1Combo() {
        this.player1Combo = 0;
    }
    
    public int getPlayer2Combo() {
        return player2Combo;
    }
    
    public void setPlayer2Combo(int player2Combo) {
        this.player2Combo = player2Combo;
    }
    
    public void resetPlayer2Combo() {
        this.player2Combo = 0;
    }
    
    public boolean isPlayer1BedAlive() {
        return player1BedAlive;
    }
    
    public void setPlayer1BedAlive(boolean player1BedAlive) {
        this.player1BedAlive = player1BedAlive;
    }
    
    public boolean isPlayer2BedAlive() {
        return player2BedAlive;
    }
    
    public void setPlayer2BedAlive(boolean player2BedAlive) {
        this.player2BedAlive = player2BedAlive;
    }
    
    // Métodos auxiliares para obter estatísticas por jogador
    public int getKills(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Kills;
        } else if (player2Id.equals(playerId)) {
            return player2Kills;
        }
        return 0;
    }
    
    public int getGoals(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Goals;
        } else if (player2Id.equals(playerId)) {
            return player2Goals;
        }
        return 0;
    }
    
    public int getLives(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Lives;
        } else if (player2Id.equals(playerId)) {
            return player2Lives;
        }
        return 0;
    }
    
    public int getHits(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Hits;
        } else if (player2Id.equals(playerId)) {
            return player2Hits;
        }
        return 0;
    }
    
    public int getCombo(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Combo;
        } else if (player2Id.equals(playerId)) {
            return player2Combo;
        }
        return 0;
    }
    
    public boolean isBedAlive(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1BedAlive;
        } else if (player2Id.equals(playerId)) {
            return player2BedAlive;
        }
        return false;
    }
    
    // Getters e Setters para sopas
    public int getPlayer1Sopas() {
        return player1Sopas;
    }
    
    public void setPlayer1Sopas(int player1Sopas) {
        this.player1Sopas = player1Sopas;
    }
    
    public void addPlayer1Sopa() {
        this.player1Sopas++;
    }
    
    public int getPlayer2Sopas() {
        return player2Sopas;
    }
    
    public void setPlayer2Sopas(int player2Sopas) {
        this.player2Sopas = player2Sopas;
    }
    
    public void addPlayer2Sopa() {
        this.player2Sopas++;
    }
    
    // Getters e Setters para streaks
    public int getPlayer1Streak() {
        return player1Streak;
    }
    
    public void setPlayer1Streak(int player1Streak) {
        this.player1Streak = player1Streak;
    }
    
    public void addPlayer1Streak() {
        this.player1Streak++;
    }
    
    public void resetPlayer1Streak() {
        this.player1Streak = 0;
    }
    
    public int getPlayer2Streak() {
        return player2Streak;
    }
    
    public void setPlayer2Streak(int player2Streak) {
        this.player2Streak = player2Streak;
    }
    
    public void addPlayer2Streak() {
        this.player2Streak++;
    }
    
    public void resetPlayer2Streak() {
        this.player2Streak = 0;
    }
    
    // Métodos auxiliares para obter estatísticas por jogador
    public int getSopas(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Sopas;
        } else if (player2Id.equals(playerId)) {
            return player2Sopas;
        }
        return 0;
    }
    
    public int getStreak(UUID playerId) {
        if (player1Id.equals(playerId)) {
            return player1Streak;
        } else if (player2Id.equals(playerId)) {
            return player2Streak;
        }
        return 0;
    }
}

