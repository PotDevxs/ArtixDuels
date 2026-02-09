package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderManager {
    private DuelManager duelManager;
    private StatsManager statsManager;
    private ArtixDuels plugin;

    public PlaceholderManager(DuelManager duelManager, StatsManager statsManager) {
        this.duelManager = duelManager;
        this.statsManager = statsManager;
        this.plugin = null;
    }

    public PlaceholderManager(ArtixDuels plugin, DuelManager duelManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.statsManager = statsManager;
    }

    public String processPlaceholders(String text, Player player, DuelMode mode) {
        return processPlaceholders(text, player, null, mode);
    }

    public String processPlaceholders(String text, Player player, Player opponent, DuelMode mode) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Placeholders gerais
        if (player != null) {
            placeholders.put("player", player.getName());
            placeholders.put("ping", String.valueOf(getPing(player)));
        }
        
        if (opponent != null) {
            placeholders.put("opponent", opponent.getName());
            placeholders.put("opponentping", String.valueOf(getPing(opponent)));
        }
        
        // Placeholders por modo
        if (mode != null) {
            placeholders.put("mode", mode.getDisplayName());
            placeholders.put("mode-name", mode.getName());
            placeholders.put("players-in-duel-mode", String.valueOf(duelManager.getActiveDuelsCountByMode(mode) * 2));
            placeholders.put("players-in-queue-mode", String.valueOf(duelManager.getMatchmakingQueueSizeByMode(mode)));
            placeholders.put("active-duels-mode", String.valueOf(duelManager.getActiveDuelsCountByMode(mode)));
            placeholders.put("total-players-mode", String.valueOf(
                duelManager.getActiveDuelsCountByMode(mode) * 2 + duelManager.getMatchmakingQueueSizeByMode(mode)));
        }
        
        // Placeholders gerais de duelo
        placeholders.put("players-in-duel", String.valueOf(duelManager.getActiveDuelsCount() * 2));
        placeholders.put("players-in-queue", String.valueOf(duelManager.getMatchmakingQueueSize()));
        placeholders.put("active-duels", String.valueOf(duelManager.getActiveDuelsCount()));
        placeholders.put("total-players", String.valueOf(
            duelManager.getActiveDuelsCount() * 2 + duelManager.getMatchmakingQueueSize()));
        placeholders.put("online-players", String.valueOf(
            org.bukkit.Bukkit.getOnlinePlayers().size()));
        
        // Placeholders de estatísticas
        if (player != null) {
            dev.artix.artixduels.models.PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats != null) {
                placeholders.put("elo", String.valueOf(stats.getElo()));
                placeholders.put("wins", String.valueOf(stats.getWins()));
                placeholders.put("losses", String.valueOf(stats.getLosses()));
                placeholders.put("draws", String.valueOf(stats.getDraws()));
                placeholders.put("winrate", String.format("%.2f", stats.getWinRate()));
                placeholders.put("winstreak", String.valueOf(stats.getWinStreak()));
                placeholders.put("beststreak", String.valueOf(stats.getBestWinStreak()));
                
                // Placeholders por modo específico
                for (DuelMode duelMode : DuelMode.values()) {
                    String modeName = duelMode.getName().toLowerCase();
                    dev.artix.artixduels.models.PlayerStats.ModeStats modeStats = stats.getModeStats(duelMode);
                    
                    placeholders.put(modeName + "-wins", String.valueOf(modeStats.getWins()));
                    placeholders.put(modeName + "-losses", String.valueOf(modeStats.getLosses()));
                    placeholders.put(modeName + "-kills", String.valueOf(modeStats.getKills()));
                    placeholders.put(modeName + "-draws", String.valueOf(0)); // Por enquanto 0
                    
                    // Placeholders com nome completo do modo
                    String modeDisplayName = duelMode.getDisplayName().toLowerCase().replace(" ", "");
                    placeholders.put(modeDisplayName + "-wins", String.valueOf(modeStats.getWins()));
                    placeholders.put(modeDisplayName + "-losses", String.valueOf(modeStats.getLosses()));
                    placeholders.put(modeDisplayName + "-kills", String.valueOf(modeStats.getKills()));
                }
            }
        }
        
        // Placeholders de ranking
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.RankingManager rankingManager = plugin.getRankingManager();
                if (rankingManager != null) {
                    int globalRank = rankingManager.getPlayerRank(player.getUniqueId(), "elo");
                    placeholders.put("global-rank", String.valueOf(globalRank));
                    placeholders.put("global-rank-formatted", formatRank(globalRank));
                    
                    if (mode != null) {
                        int modeRank = rankingManager.getPlayerRank(player.getUniqueId(), "mode-" + mode.getName().toLowerCase());
                        placeholders.put("mode-rank", String.valueOf(modeRank));
                        placeholders.put("mode-rank-formatted", formatRank(modeRank));
                    }
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Placeholders de desafios
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.ChallengeManager challengeManager = plugin.getChallengeManager();
                if (challengeManager != null) {
                    int activeChallenges = challengeManager.getActiveChallenges(player.getUniqueId(), 
                        dev.artix.artixduels.models.Challenge.ChallengeType.DAILY).size() +
                        challengeManager.getActiveChallenges(player.getUniqueId(), 
                        dev.artix.artixduels.models.Challenge.ChallengeType.WEEKLY).size();
                    placeholders.put("active-challenges", String.valueOf(activeChallenges));
                    placeholders.put("completed-challenges", String.valueOf(0));
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Placeholders de torneios
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.TournamentManager tournamentManager = plugin.getTournamentManager();
                if (tournamentManager != null) {
                    dev.artix.artixduels.models.Tournament tournament = tournamentManager.getPlayerTournament(player.getUniqueId());
                    if (tournament != null) {
                        placeholders.put("tournament-name", tournament.getName());
                        placeholders.put("tournament-state", tournament.getState().toString());
                    } else {
                        placeholders.put("tournament-name", "Nenhum");
                        placeholders.put("tournament-state", "NONE");
                    }
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Placeholders de cosméticos
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.TitleManager titleManager = plugin.getTitleManager();
                if (titleManager != null) {
                    String activeTitle = titleManager.getActiveTitleDisplay(player.getUniqueId());
                    placeholders.put("active-title", activeTitle != null && !activeTitle.isEmpty() ? activeTitle : "Nenhum");
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Placeholders de conquistas
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.AchievementManager achievementManager = plugin.getAchievementManager();
                if (achievementManager != null) {
                    Map<String, dev.artix.artixduels.models.AchievementProgress> progress = achievementManager.getPlayerProgress(player.getUniqueId());
                    int unlocked = 0;
                    for (dev.artix.artixduels.models.AchievementProgress p : progress.values()) {
                        if (p.isUnlocked()) unlocked++;
                    }
                    placeholders.put("unlocked-achievements", String.valueOf(unlocked));
                    placeholders.put("total-achievements", String.valueOf(achievementManager.getAchievements().size()));
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Placeholders de títulos
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.TitleManager titleManager = plugin.getTitleManager();
                if (titleManager != null) {
                    String activeTitle = titleManager.getActiveTitleDisplay(player.getUniqueId());
                    placeholders.put("active-title", activeTitle != null && !activeTitle.isEmpty() ? activeTitle : "Nenhum");
                    placeholders.put("unlocked-titles", String.valueOf(titleManager.getUnlockedTitles(player.getUniqueId()).size()));
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        // Processar placeholders
        String processed = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        if (player != null && plugin != null) {
            try {
                dev.artix.artixduels.managers.ThemeManager themeManager = plugin.getThemeManager();
                if (themeManager != null) {
                    String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                    processed = processed.replace("<theme>", themeColor);
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        } else {
            // Se não houver jogador, usar cor padrão
            processed = processed.replace("<theme>", "&f");
        }
        
        // Processar placeholders do PlaceholderAPI se disponível
        if (plugin != null && player != null) {
            try {
                dev.artix.artixduels.utils.IntegrationManager integrationManager = plugin.getIntegrationManager();
                if (integrationManager != null && integrationManager.isPlaceholderAPIEnabled()) {
                    processed = integrationManager.processPlaceholders(processed, player);
                }
            } catch (Exception e) {
                // Ignorar erros de PlaceholderAPI
            }
        }
        
        return processed;
    }
    
    private String formatRank(int rank) {
        if (rank <= 0) return "N/A";
        if (rank == 1) return "1º";
        if (rank == 2) return "2º";
        if (rank == 3) return "3º";
        return rank + "º";
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }
}
