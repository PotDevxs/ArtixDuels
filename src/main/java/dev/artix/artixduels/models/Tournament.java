package dev.artix.artixduels.models;

import org.bukkit.configuration.ConfigurationSection;
import java.util.*;

public class Tournament {
    private String id;
    private String name;
    private String description;
    private TournamentType type;
    private TournamentState state;
    private int maxParticipants;
    private int minParticipants;
    private List<UUID> participants;
    private Map<UUID, TournamentParticipant> participantData;
    private Map<Integer, List<TournamentMatch>> brackets;
    private int currentRound;
    private long startTime;
    private long endTime;
    private Map<String, Object> rewards;
    private String mode;
    private String kit;
    public Tournament(String id, String name, String description, TournamentType type,
                     int maxParticipants, int minParticipants, String mode, String kit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.state = TournamentState.REGISTRATION;
        this.maxParticipants = maxParticipants;
        this.minParticipants = minParticipants;
        this.participants = new ArrayList<>();
        this.participantData = new HashMap<>();
        this.brackets = new HashMap<>();
        this.currentRound = 0;
        this.startTime = 0;
        this.endTime = 0;
        this.rewards = new HashMap<>();
        this.mode = mode;
        this.kit = kit;
    }
    public static Tournament fromConfig(ConfigurationSection section) {
        String id = section.getName();
        String name = section.getString("name", "Torneio");
        String description = section.getString("description", "");
        TournamentType type = TournamentType.fromString(section.getString("type", "SINGLE_ELIMINATION"));
        int maxParticipants = section.getInt("max-participants", 16);
        int minParticipants = section.getInt("min-participants", 4);
        String mode = section.getString("mode", "BEDFIGHT");
        String kit = section.getString("kit", "");
        Tournament tournament = new Tournament(id, name, description, type, maxParticipants, minParticipants, mode, kit);
        if (section.contains("rewards")) {
            ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String key : rewardsSection.getKeys(false)) {
                    tournament.rewards.put(key, rewardsSection.get(key));
                }
            }
        }
        return tournament;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public TournamentType getType() {
        return type;
    }
    public TournamentState getState() {
        return state;
    }
    public void setState(TournamentState state) {
        this.state = state;
    }
    public int getMaxParticipants() {
        return maxParticipants;
    }
    public int getMinParticipants() {
        return minParticipants;
    }
    public List<UUID> getParticipants() {
        return participants;
    }
    public void addParticipant(UUID playerId) {
        if (!participants.contains(playerId)) {
            participants.add(playerId);
            participantData.put(playerId, new TournamentParticipant(playerId));
        }
    }
    public void removeParticipant(UUID playerId) {
        participants.remove(playerId);
        participantData.remove(playerId);
    }
    public boolean isParticipant(UUID playerId) {
        return participants.contains(playerId);
    }
    public Map<UUID, TournamentParticipant> getParticipantData() {
        return participantData;
    }
    public TournamentParticipant getParticipantData(UUID playerId) {
        return participantData.get(playerId);
    }
    public Map<Integer, List<TournamentMatch>> getBrackets() {
        return brackets;
    }
    public void setBrackets(Map<Integer, List<TournamentMatch>> brackets) {
        this.brackets = brackets;
    }
    public int getCurrentRound() {
        return currentRound;
    }
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
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
    public Map<String, Object> getRewards() {
        return rewards;
    }
    public void setRewards(Map<String, Object> rewards) {
        this.rewards = rewards;
    }
    public String getMode() {
        return mode;
    }
    public String getKit() {
        return kit;
    }
    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }
    public boolean hasMinParticipants() {
        return participants.size() >= minParticipants;
    }
    public enum TournamentType {
        SINGLE_ELIMINATION("single_elimination"),
        DOUBLE_ELIMINATION("double_elimination");
        private String name;
        TournamentType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public static TournamentType fromString(String name) {
            for (TournamentType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return SINGLE_ELIMINATION;
        }
    }
    public enum TournamentState {
        REGISTRATION("registration"),
        STARTING("starting"),
        IN_PROGRESS("in_progress"),
        FINISHED("finished"),
        CANCELLED("cancelled");

        private String name;
        TournamentState(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public static TournamentState fromString(String name) {
            for (TournamentState state : values()) {
                if (state.name.equalsIgnoreCase(name)) {
                    return state;
                }
            }
            return REGISTRATION;
        }
    }
}
