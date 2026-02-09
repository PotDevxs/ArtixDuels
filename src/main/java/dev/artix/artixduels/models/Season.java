package dev.artix.artixduels.models;

import java.util.Map;
import java.util.UUID;

/**
 * Representa uma temporada competitiva.
 */
public class Season {
    private String id;
    private String name;
    private int seasonNumber;
    private long startTime;
    private long endTime;
    private boolean active;
    private UUID topPlayerId;
    private String topPlayerName;
    private Map<UUID, Integer> seasonElo;
    private Map<UUID, Integer> seasonWins;
    private Map<String, Object> rewards;

    public Season(String id, String name, long startTime, long endTime) {
        this.id = id;
        this.name = name;
        this.seasonNumber = 1;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = false;
    }

    public Season(int seasonNumber, long startTime2, long endTime2) {
        this.seasonNumber = seasonNumber;
        this.id = "season_" + seasonNumber;
        this.name = "Temporada " + seasonNumber;
        this.startTime = startTime2;
        this.endTime = endTime2;
        this.active = true;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<UUID, Integer> getSeasonElo() {
        return seasonElo;
    }

    public void setSeasonElo(Map<UUID, Integer> seasonElo) {
        this.seasonElo = seasonElo;
    }

    public Map<UUID, Integer> getSeasonWins() {
        return seasonWins;
    }

    public void setSeasonWins(Map<UUID, Integer> seasonWins) {
        this.seasonWins = seasonWins;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }

    public void setTopPlayerId(UUID topPlayerId) {
        this.topPlayerId = topPlayerId;
    }

    public void setTopPlayerName(String topPlayerName) {
        this.topPlayerName = topPlayerName;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public UUID getTopPlayerId() {
        return topPlayerId;
    }

    public String getTopPlayerName() {
        return topPlayerName;
    }
}
