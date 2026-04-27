package dev.artix.artixduels.models;

import java.util.UUID;

public class TournamentParticipant {
    private UUID playerId;
    private int wins;
    private int losses;
    private boolean eliminated;
    private int currentRound;
    private int position;
    public TournamentParticipant(UUID playerId) {
        this.playerId = playerId;
        this.wins = 0;
        this.losses = 0;
        this.eliminated = false;
        this.currentRound = 0;
        this.position = 0;
    }
    public UUID getPlayerId() {
        return playerId;
    }
    public int getWins() {
        return wins;
    }
    public void addWin() {
        this.wins++;
    }
    public int getLosses() {
        return losses;
    }
    public void addLoss() {
        this.losses++;
        if (this.losses >= 2) {
            this.eliminated = true;
        }
    }
    public boolean isEliminated() {
        return eliminated;
    }
    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
    public int getCurrentRound() {
        return currentRound;
    }
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
}
