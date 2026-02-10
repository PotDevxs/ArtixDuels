package dev.artix.artixduels.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsDAO implements IStatsDAO {
    private MongoCollection<Document> collection;

    public StatsDAO(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public PlayerStats getPlayerStats(UUID playerId) {
        Document doc = collection.find(Filters.eq("playerId", playerId.toString())).first();
        if (doc == null) {
            return null;
        }
        return documentToStats(doc);
    }

    public void savePlayerStats(PlayerStats stats) {
        Document doc = statsToDocument(stats);
        collection.replaceOne(
            Filters.eq("playerId", stats.getPlayerId().toString()),
            doc,
            new ReplaceOptions().upsert(true)
        );
    }

    public void createPlayerStats(PlayerStats stats) {
        Document doc = statsToDocument(stats);
        collection.insertOne(doc);
    }

    @Override
    public java.util.List<PlayerStats> getAllPlayerStats() {
        java.util.List<PlayerStats> allStats = new java.util.ArrayList<>();
        for (Document doc : collection.find()) {
            PlayerStats stats = documentToStats(doc);
            if (stats != null) {
                allStats.add(stats);
            }
        }
        return allStats;
    }

    private Document statsToDocument(PlayerStats stats) {
        Document doc = new Document();
        doc.append("playerId", stats.getPlayerId().toString());
        doc.append("playerName", stats.getPlayerName());
        doc.append("wins", stats.getWins());
        doc.append("losses", stats.getLosses());
        doc.append("draws", stats.getDraws());
        doc.append("elo", stats.getElo());
        doc.append("winStreak", stats.getWinStreak());
        doc.append("bestWinStreak", stats.getBestWinStreak());
        doc.append("xp", stats.getXp());
        doc.append("level", stats.getLevel());
        
        // Salvar estatísticas por modo
        Document modeStatsDoc = new Document();
        if (stats.getModeStats() != null) {
            for (Map.Entry<DuelMode, PlayerStats.ModeStats> entry : stats.getModeStats().entrySet()) {
                Document modeDoc = new Document();
                modeDoc.append("wins", entry.getValue().getWins());
                modeDoc.append("losses", entry.getValue().getLosses());
                modeDoc.append("kills", entry.getValue().getKills());
                modeStatsDoc.append(entry.getKey().getName(), modeDoc);
            }
        }
        doc.append("modeStats", modeStatsDoc);
        
        return doc;
    }

    private PlayerStats documentToStats(Document doc) {
        String playerIdStr = doc.getString("playerId");
        if (playerIdStr == null) return null;
        PlayerStats stats = new PlayerStats();
        stats.setPlayerId(UUID.fromString(playerIdStr));
        stats.setPlayerName(doc.getString("playerName"));
        stats.setWins(doc.getInteger("wins", 0));
        stats.setLosses(doc.getInteger("losses", 0));
        stats.setDraws(doc.getInteger("draws", 0));
        stats.setElo(doc.getInteger("elo", 1000));
        stats.setWinStreak(doc.getInteger("winStreak", 0));
        stats.setBestWinStreak(doc.getInteger("bestWinStreak", 0));
        stats.setXp(doc.getInteger("xp", 0));
        stats.setLevel(doc.getInteger("level", 1));
        
        // Carregar estatísticas por modo
        Map<DuelMode, PlayerStats.ModeStats> modeStatsMap = new HashMap<>();
        if (doc.containsKey("modeStats")) {
            Document modeStatsDoc = (Document) doc.get("modeStats");
            
            for (DuelMode mode : DuelMode.values()) {
                if (modeStatsDoc.containsKey(mode.getName())) {
                    Document modeDoc = (Document) modeStatsDoc.get(mode.getName());
                    PlayerStats.ModeStats modeStat = new PlayerStats.ModeStats();
                    modeStat.setWins(modeDoc.getInteger("wins", 0));
                    modeStat.setLosses(modeDoc.getInteger("losses", 0));
                    modeStat.setKills(modeDoc.getInteger("kills", 0));
                    modeStatsMap.put(mode, modeStat);
                } else {
                    modeStatsMap.put(mode, new PlayerStats.ModeStats());
                }
            }
        } else {
            // Inicializar com estatísticas vazias se não existir
            for (DuelMode mode : DuelMode.values()) {
                modeStatsMap.put(mode, new PlayerStats.ModeStats());
            }
        }
        stats.setModeStats(modeStatsMap);
        
        return stats;
    }
}

