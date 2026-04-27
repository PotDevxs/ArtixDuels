package dev.artix.artixduels.models;

import java.util.UUID;

public class TournamentMatch {
    private UUID matchId;
    private UUID player1Id;
    private UUID player2Id;
    private UUID winnerId;
    private UUID loserId;
    private int round;
    private int matchNumber;
    private MatchState state;
    private long startTime;
    private long endTime;

    public TournamentMatch(UUID player1Id, UUID player2Id, int round, int matchNumber) {
        this.matchId = UUID.randomUUID();
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.winnerId = null;
        this.loserId = null;
        this.round = round;
        this.matchNumber = matchNumber;
        this.state = MatchState.PENDING;
        this.startTime = 0;
        this.endTime = 0;
    }
    public UUID getMatchId() {
        return matchId;
    }
    public UUID getPlayer1Id() {
        return player1Id;
    }
    public UUID getPlayer2Id() {
        return player2Id;
    }
    public UUID getWinnerId() {
        return winnerId;
    }
    public void setWinner(UUID winnerId) {
        this.winnerId = winnerId;
        this.loserId = winnerId.equals(player1Id) ? player2Id : player1Id;
        this.state = MatchState.FINISHED;
        this.endTime = System.currentTimeMillis();
    }
    public UUID getLoserId() {
        return loserId;
    }
    public int getRound() {
        return round;
    }
    public int getMatchNumber() {
        return matchNumber;
    }
    public MatchState getState() {
        return state;
    }
    public void setState(MatchState state) {
        this.state = state;
        if (state == MatchState.IN_PROGRESS) {
            this.startTime = System.currentTimeMillis();
        }
    }
    public long getStartTime() {
        return startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public boolean isPlayerInMatch(UUID playerId) {
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
    public enum MatchState {
        PENDING("pending"),
        IN_PROGRESS("in_progress"),
        FINISHED("finished"),
        BYE("bye");
        private String name;
        MatchState(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
}

