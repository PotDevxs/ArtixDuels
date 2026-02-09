package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private Map<UUID, Scoreboard> playerScoreboards;
    private StatsManager statsManager;
    private PlaceholderManager placeholderManager;
    private PlayerScoreboardPreferences preferences;
    private FileConfiguration scoreboardConfig;
    private boolean enabled;
    private boolean showInLobby;
    private boolean showInDuel;
    private String lobbyTitle;
    private List<String> lobbyLines;
    private List<String> lobbyModeTemplate;
    private String duelTitle;
    private List<String> duelLines;
    private Map<dev.artix.artixduels.models.DuelMode, List<String>> modeDuelLines;
    private Map<String, String> globalPlaceholders;
    private String queueTitle;
    private List<String> queueLines;
    private dev.artix.artixduels.managers.ThemeManager themeManager;

    public ScoreboardManager(StatsManager statsManager, FileConfiguration scoreboardConfig, PlaceholderManager placeholderManager, PlayerScoreboardPreferences preferences) {
        this.statsManager = statsManager;
        this.scoreboardConfig = scoreboardConfig;
        this.placeholderManager = placeholderManager;
        this.preferences = preferences;
        this.playerScoreboards = new HashMap<>();
        loadConfig();
    }

    public void setThemeManager(dev.artix.artixduels.managers.ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    private void loadConfig() {
        ConfigurationSection scoreboardSection = scoreboardConfig.getConfigurationSection("scoreboard");
        if (scoreboardSection != null) {
            enabled = scoreboardSection.getBoolean("enabled", true);
            showInLobby = scoreboardSection.getBoolean("show-in-lobby", true);
            showInDuel = scoreboardSection.getBoolean("show-in-duel", true);
        }

        ConfigurationSection lobbySection = scoreboardConfig.getConfigurationSection("lobby");
        if (lobbySection != null) {
            lobbyTitle = ChatColor.translateAlternateColorCodes('&', lobbySection.getString("title", "§6§lARTIX DUELS"));
            lobbyLines = lobbySection.getStringList("lines");
        }
        
        ConfigurationSection modeTemplateSection = scoreboardConfig.getConfigurationSection("mode-template");
        if (modeTemplateSection != null) {
            lobbyModeTemplate = modeTemplateSection.getStringList("lines");
        }

        ConfigurationSection duelSection = scoreboardConfig.getConfigurationSection("duel");
        if (duelSection != null) {
            duelTitle = ChatColor.translateAlternateColorCodes('&', duelSection.getString("title", "§6§lDUELO"));
            duelLines = duelSection.getStringList("lines");
            
            // Carregar placeholders globais
            globalPlaceholders = new HashMap<>();
            ConfigurationSection placeholdersSection = duelSection.getConfigurationSection("placeholders");
            if (placeholdersSection != null) {
                for (String key : placeholdersSection.getKeys(false)) {
                    globalPlaceholders.put(key, placeholdersSection.getString(key));
                }
            }
            
            // Carregar scoreboards específicas por modo
            modeDuelLines = new HashMap<>();
            ConfigurationSection modesSection = duelSection.getConfigurationSection("modes");
            if (modesSection != null) {
                for (String modeName : modesSection.getKeys(false)) {
                    dev.artix.artixduels.models.DuelMode mode = dev.artix.artixduels.models.DuelMode.fromString(modeName.toUpperCase());
                    // Mapear SOPA para SOUP
                    if (mode == null && modeName.equalsIgnoreCase("SOPA")) {
                        mode = dev.artix.artixduels.models.DuelMode.SOUP;
                    }
                    if (mode != null) {
                        List<String> lines = modesSection.getStringList(modeName);
                        if (lines != null && !lines.isEmpty()) {
                            modeDuelLines.put(mode, lines);
                        }
                    }
                }
            }
        }

        ConfigurationSection queueSection = scoreboardConfig.getConfigurationSection("queue");
        if (queueSection != null) {
            queueTitle = ChatColor.translateAlternateColorCodes('&', queueSection.getString("title", "§6§lFILA DE MATCHMAKING"));
            queueLines = queueSection.getStringList("lines");
        }
    }

    public void createDuelScoreboard(Player player1, Player player2, Duel duel) {
        if (!enabled || !showInDuel) return;
        
        createDuelScoreboardForPlayer(player1, player2, duel);
        createDuelScoreboardForPlayer(player2, player1, duel);
    }

    private void createDuelScoreboardForPlayer(Player player, Player opponent, Duel duel) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("duel", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(duelTitle != null ? duelTitle : ChatColor.GOLD + "§6§lDUELO");

        // Verificar se existe scoreboard específica para o modo
        List<String> linesToUse = null;
        if (modeDuelLines != null) {
            linesToUse = modeDuelLines.get(duel.getMode());
        }
        
        if (linesToUse == null || linesToUse.isEmpty()) {
            linesToUse = duelLines;
        }

        if (linesToUse != null && !linesToUse.isEmpty()) {
            int score = linesToUse.size();
            for (int i = 0; i < linesToUse.size(); i++) {
                String line = linesToUse.get(i);
                String processedLine = processPlaceholders(line, player, opponent, duel);
                
                // Corrigir espaçamento: linhas vazias ou com apenas espaços precisam ser únicas
                String trimmed = ChatColor.stripColor(processedLine).trim();
                if (trimmed.isEmpty() || trimmed.equals("&f") || trimmed.equals("&7")) {
                    // Criar linha vazia única usando caracteres invisíveis únicos
                    char invisibleChar = (char) (0xE000 + i);
                    processedLine = ChatColor.RESET.toString() + invisibleChar;
                }
                
                if (processedLine.length() > 40) {
                    processedLine = processedLine.substring(0, 40);
                }
                
                // Garantir que cada linha seja única adicionando um identificador invisível
                char uniqueChar = (char) (0xE000 + i);
                String finalLine = processedLine + uniqueChar;
                
                objective.getScore(finalLine).setScore(score);
                score--;
            }
        } else {
            Team playerTeam = scoreboard.registerNewTeam("player");
            playerTeam.addEntry(ChatColor.GREEN + "");
            playerTeam.setPrefix(ChatColor.GREEN + "Você: ");
            playerTeam.setSuffix(ChatColor.WHITE + player.getName());

            Team opponentTeam = scoreboard.registerNewTeam("opponent");
            opponentTeam.addEntry(ChatColor.RED + "");
            opponentTeam.setPrefix(ChatColor.RED + "Oponente: ");
            opponentTeam.setSuffix(ChatColor.WHITE + opponent.getName());

            Score empty1 = objective.getScore(" ");
            empty1.setScore(6);

            Score kitScore = objective.getScore(ChatColor.YELLOW + "Kit: " + ChatColor.WHITE + duel.getKitName());
            kitScore.setScore(5);

            Score arenaScore = objective.getScore(ChatColor.YELLOW + "Arena: " + ChatColor.WHITE + duel.getArenaName());
            arenaScore.setScore(4);

            Score empty2 = objective.getScore("  ");
            empty2.setScore(3);

            Score stateScore = objective.getScore(ChatColor.GOLD + "Estado: " + getStateText(duel.getState()));
            stateScore.setScore(2);

            Score empty3 = objective.getScore("   ");
            empty3.setScore(1);
        }

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }

    public void createLobbyScoreboard(Player player) {
        if (!enabled || !showInLobby) return;
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("lobby", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(lobbyTitle != null ? lobbyTitle : ChatColor.GOLD + "§6§lARTIX DUELS");

        PlayerStats stats = statsManager.getPlayerStats(player);
        
        // Construir linhas dinamicamente
        List<String> allLines = new java.util.ArrayList<>();
        
        if (lobbyLines != null && !lobbyLines.isEmpty()) {
            // Processar cada linha do lobby
            for (String line : lobbyLines) {
                // Verificar se a linha contém <mode-template>
                if (line.contains("<mode-template>")) {
                    // Substituir <mode-template> pelas linhas do modo ativo
                    if (preferences != null && lobbyModeTemplate != null && !lobbyModeTemplate.isEmpty()) {
                        dev.artix.artixduels.models.DuelMode activeMode = preferences.getPlayerActiveMode(player.getUniqueId());
                        
                        if (activeMode != null) {
                            for (String templateLine : lobbyModeTemplate) {
                                String processedTemplateLine = processModeTemplate(templateLine, activeMode, stats);
                                allLines.add(processedTemplateLine);
                            }
                        }
                    }
                } else {
                    // Linha normal, processar placeholders
                    allLines.add(line);
                }
            }
        }
        
        // Processar e adicionar linhas ao scoreboard
        if (!allLines.isEmpty()) {
            int score = allLines.size();
            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);
                String processedLine = processLobbyPlaceholders(line, player, stats);
                
                // Corrigir espaçamento: linhas vazias ou com apenas espaços precisam ser únicas
                String trimmed = ChatColor.stripColor(processedLine).trim();
                if (trimmed.isEmpty() || trimmed.equals("&f") || trimmed.equals("&7")) {
                    // Criar linha vazia única usando caracteres invisíveis únicos
                    char invisibleChar = (char) (0xE000 + i);
                    processedLine = ChatColor.RESET.toString() + invisibleChar;
                }
                
                if (processedLine.length() > 40) {
                    processedLine = processedLine.substring(0, 40);
                }
                
                // Garantir que cada linha seja única adicionando um identificador invisível
                char uniqueChar = (char) (0xE000 + i);
                String finalLine = processedLine + uniqueChar;
                
                objective.getScore(finalLine).setScore(score);
                score--;
            }
        } else {
            Score empty1 = objective.getScore(" ");
            empty1.setScore(8);

            Score eloScore = objective.getScore(ChatColor.YELLOW + "Elo: " + ChatColor.WHITE + stats.getElo());
            eloScore.setScore(7);

            Score empty2 = objective.getScore("  ");
            empty2.setScore(6);

            Score winsScore = objective.getScore(ChatColor.GREEN + "Vitórias: " + ChatColor.WHITE + stats.getWins());
            winsScore.setScore(5);

            Score lossesScore = objective.getScore(ChatColor.RED + "Derrotas: " + ChatColor.WHITE + stats.getLosses());
            lossesScore.setScore(4);

            Score empty3 = objective.getScore("   ");
            empty3.setScore(3);

            double winRate = stats.getWinRate();
            Score winRateScore = objective.getScore(ChatColor.AQUA + "Win Rate: " + ChatColor.WHITE + String.format("%.2f", winRate) + "%");
            winRateScore.setScore(2);

            Score empty4 = objective.getScore("    ");
            empty4.setScore(1);
        }

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }

    public void createQueueScoreboard(Player player, dev.artix.artixduels.models.DuelMode mode) {
        if (!enabled) return;
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("queue", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(queueTitle != null ? queueTitle : ChatColor.GOLD + "§6§lFILA DE MATCHMAKING");

        PlayerStats stats = statsManager.getPlayerStats(player);
        
        if (queueLines != null && !queueLines.isEmpty()) {
            int score = queueLines.size();
            for (int i = 0; i < queueLines.size(); i++) {
                String line = queueLines.get(i);
                String processedLine = processQueuePlaceholders(line, player, stats, mode);
                
                // Corrigir espaçamento: linhas vazias ou com apenas espaços precisam ser únicas
                String trimmed = ChatColor.stripColor(processedLine).trim();
                if (trimmed.isEmpty() || trimmed.equals("&f") || trimmed.equals("&7")) {
                    // Criar linha vazia única usando caracteres invisíveis únicos
                    char invisibleChar = (char) (0xE000 + i);
                    processedLine = ChatColor.RESET.toString() + invisibleChar;
                }
                
                if (processedLine.length() > 40) {
                    processedLine = processedLine.substring(0, 40);
                }
                
                // Garantir que cada linha seja única adicionando um identificador invisível
                char uniqueChar = (char) (0xE000 + i);
                String finalLine = processedLine + uniqueChar;
                
                objective.getScore(finalLine).setScore(score);
                score--;
            }
        } else {
            // Fallback padrão
            Score modeScore = objective.getScore(ChatColor.YELLOW + "Modo: " + ChatColor.WHITE + mode.getDisplayName());
            modeScore.setScore(5);
            
            Score empty1 = objective.getScore(ChatColor.BLACK + " ");
            empty1.setScore(4);
            
            Score eloScore = objective.getScore(ChatColor.GOLD + "ELO: " + ChatColor.WHITE + stats.getElo());
            eloScore.setScore(3);
            
            Score empty2 = objective.getScore(ChatColor.DARK_BLUE + " ");
            empty2.setScore(2);
            
            Score waitingScore = objective.getScore(ChatColor.GRAY + "Aguardando oponente...");
            waitingScore.setScore(1);
        }

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }

    private String processQueuePlaceholders(String line, Player player, PlayerStats stats, dev.artix.artixduels.models.DuelMode mode) {
        String processed = ChatColor.translateAlternateColorCodes('&', line);
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        if (player != null && themeManager != null) {
            try {
                String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                processed = processed.replace("<theme>", themeColor);
            } catch (Exception e) {
                processed = processed.replace("<theme>", "&f");
            }
        } else {
            processed = processed.replace("<theme>", "&f");
        }
        
        // Substituir placeholders específicos da queue
        processed = processed.replace("<queue-mode>", mode.getDisplayName());
        processed = processed.replace("<elo>", String.valueOf(stats.getElo()));
        
        // Processar placeholders adicionais usando PlaceholderManager
        if (placeholderManager != null) {
            processed = placeholderManager.processPlaceholders(processed, player, null, mode);
        } else {
            // Fallback para compatibilidade
            processed = processed.replace("{player}", player.getName());
            processed = processed.replace("{elo}", String.valueOf(stats.getElo()));
            processed = processed.replace("{wins}", String.valueOf(stats.getWins()));
            processed = processed.replace("{losses}", String.valueOf(stats.getLosses()));
            processed = processed.replace("{winrate}", String.format("%.2f", stats.getWinRate()));
        }
        
        return processed;
    }

    private String processModeTemplate(String templateLine, dev.artix.artixduels.models.DuelMode mode, PlayerStats stats) {
        String processed = templateLine;
        
        // Substituir placeholders do modo
        processed = processed.replace("<kit-mode>", mode.getDisplayName());
        processed = processed.replace("<wins-mode>", String.valueOf(stats.getModeStats(mode).getWins()));
        processed = processed.replace("<loses-mode>", String.valueOf(stats.getModeStats(mode).getLosses()));
        processed = processed.replace("<kills-mode>", String.valueOf(stats.getModeStats(mode).getKills()));
        
        // Processar placeholders gerais
        processed = ChatColor.translateAlternateColorCodes('&', processed);
        
        return processed;
    }

    private String processLobbyPlaceholders(String line, Player player, PlayerStats stats) {
        String processed = ChatColor.translateAlternateColorCodes('&', line);
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        if (player != null && themeManager != null) {
            try {
                String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                processed = processed.replace("<theme>", themeColor);
            } catch (Exception e) {
                processed = processed.replace("<theme>", "&f");
            }
        } else {
            processed = processed.replace("<theme>", "&f");
        }
        
        // Substituir <players> com número de jogadores online
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        processed = processed.replace("<players>", String.valueOf(onlinePlayers));
        
        if (placeholderManager != null) {
            processed = placeholderManager.processPlaceholders(processed, player, null, null);
        } else {
            // Fallback para compatibilidade
            processed = processed.replace("{player}", player.getName());
            processed = processed.replace("{elo}", String.valueOf(stats.getElo()));
            processed = processed.replace("{wins}", String.valueOf(stats.getWins()));
            processed = processed.replace("{losses}", String.valueOf(stats.getLosses()));
            processed = processed.replace("{draws}", String.valueOf(stats.getDraws()));
            processed = processed.replace("{winrate}", String.format("%.2f", stats.getWinRate()));
            processed = processed.replace("{winstreak}", String.valueOf(stats.getWinStreak()));
            processed = processed.replace("{beststreak}", String.valueOf(stats.getBestWinStreak()));
        }
        
        return processed;
    }

    private String processPlaceholders(String line, Player player, Player opponent, Duel duel) {
        String processed = ChatColor.translateAlternateColorCodes('&', line);
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        if (player != null && themeManager != null) {
            try {
                String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                processed = processed.replace("<theme>", themeColor);
            } catch (Exception e) {
                processed = processed.replace("<theme>", "&f");
            }
        } else {
            processed = processed.replace("<theme>", "&f");
        }
        
        // Aplicar placeholders globais primeiro
        if (globalPlaceholders != null) {
            for (Map.Entry<String, String> entry : globalPlaceholders.entrySet()) {
                processed = processed.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        
        // Determinar qual jogador é B (Blue) e qual é R (Red)
        boolean isPlayer1 = duel.getPlayer1Id().equals(player.getUniqueId());
        
        // Placeholders específicos do modo
        processed = processModeSpecificPlaceholders(processed, player, opponent, duel, isPlayer1);
        
        if (placeholderManager != null) {
            processed = placeholderManager.processPlaceholders(processed, player, opponent, duel.getMode());
        } else {
            // Fallback para compatibilidade
            processed = processed.replace("{player}", player.getName());
            processed = processed.replace("{opponent}", opponent.getName());
            processed = processed.replace("{ping}", String.valueOf(getPing(player)));
            processed = processed.replace("{opponentping}", String.valueOf(getPing(opponent)));
        }
        
        processed = processed.replace("{kit}", duel.getKitName());
        processed = processed.replace("{arena}", duel.getArenaName());
        processed = processed.replace("{mode}", duel.getMode().getDisplayName());
        processed = processed.replace("{mode-name}", duel.getMode().getName());
        processed = processed.replace("{state}", getStateText(duel.getState()));
        
        return processed;
    }
    
    private String processModeSpecificPlaceholders(String line, Player player, Player opponent, Duel duel, boolean isPlayer1) {
        String processed = line;
        
        // player1 é B (Blue) e player2 é R (Red)
        UUID playerId = player.getUniqueId();
        UUID opponentId = opponent.getUniqueId();
        dev.artix.artixduels.models.DuelMode mode = duel.getMode();
        
        // Placeholders condicionais baseados no modo
        String isBedFight = (mode == dev.artix.artixduels.models.DuelMode.BEDFIGHT) ? "true" : "false";
        String isFireballFight = (mode == dev.artix.artixduels.models.DuelMode.FIREBALLFIGHT) ? "true" : "false";
        String isStickFight = (mode == dev.artix.artixduels.models.DuelMode.STICKFIGHT) ? "true" : "false";
        String isBoxing = (mode == dev.artix.artixduels.models.DuelMode.BOXING) ? "true" : "false";
        String isBattleRush = (mode == dev.artix.artixduels.models.DuelMode.BATTLERUSH) ? "true" : "false";
        boolean isSopa = (mode == dev.artix.artixduels.models.DuelMode.SOUP || mode == dev.artix.artixduels.models.DuelMode.SOUPRECRAFT);
        
        // Obter estatísticas do duelo
        int player1Kills = duel.getPlayer1Kills();
        int player2Kills = duel.getPlayer2Kills();
        int player1Goals = duel.getPlayer1Goals();
        int player2Goals = duel.getPlayer2Goals();
        int player1Lives = duel.getPlayer1Lives();
        int player2Lives = duel.getPlayer2Lives();
        int player1Hits = duel.getPlayer1Hits();
        int player2Hits = duel.getPlayer2Hits();
        boolean player1BedAlive = duel.isPlayer1BedAlive();
        boolean player2BedAlive = duel.isPlayer2BedAlive();
        int player1Sopas = duel.getPlayer1Sopas();
        int player2Sopas = duel.getPlayer2Sopas();
        int player1Streak = duel.getPlayer1Streak();
        int player2Streak = duel.getPlayer2Streak();
        
        // Placeholders para Blue (B) - player1
        String bGoal = player1Goals > 0 ? "⬤" : "○";
        String bBed = player1BedAlive ? ChatColor.translateAlternateColorCodes('&', "&a&l✓") : ChatColor.translateAlternateColorCodes('&', "&c&l✗");
        String bLives = String.valueOf(player1Lives);
        String bRed = player1BedAlive ? ChatColor.translateAlternateColorCodes('&', "&a&l✓") : ChatColor.translateAlternateColorCodes('&', "&c&l✗");
        
        // Placeholders para Red (R) - player2
        String rGoal = player2Goals > 0 ? "⬤" : "○";
        String rBed = player2BedAlive ? ChatColor.translateAlternateColorCodes('&', "&a&l✓") : ChatColor.translateAlternateColorCodes('&', "&c&l✗");
        String rLives = String.valueOf(player2Lives);
        String rSopas = String.valueOf(player2Sopas);
        
        // Placeholders para Blue (B) - player1 (sopas)
        String bSopas = String.valueOf(player1Sopas);
        
        // Placeholders para streaks
        int yourStreak = isPlayer1 ? player1Streak : player2Streak;
        String streaks = String.valueOf(yourStreak);
        
        // Placeholders gerais
        int totalKills = player1Kills + player2Kills;
        int totalGoals = player1Goals + player2Goals;
        int totalHits = player1Hits + player2Hits;
        
        // Placeholders do jogador atual
        int yourHits = duel.getHits(playerId);
        int opponentHits = duel.getHits(opponentId);
        int yourCombo = duel.getCombo(playerId);
        String comboText = yourCombo > 0 ? ChatColor.translateAlternateColorCodes('&', "&7Combo: &e" + yourCombo + "x") : "";
        
        // Substituir placeholders condicionais do modo
        processed = processed.replace("<IsBedFight>", isBedFight);
        processed = processed.replace("<IsFireballFight>", isFireballFight);
        processed = processed.replace("<IsStickFight>", isStickFight);
        processed = processed.replace("<IsBoxing>", isBoxing);
        processed = processed.replace("<IsBattleRush>", isBattleRush);
        
        // Substituir placeholders de estatísticas
        processed = processed.replace("<bGoal>", bGoal);
        processed = processed.replace("<rGoal>", rGoal);
        processed = processed.replace("<bBed>", bBed);
        processed = processed.replace("<rBed>", rBed);
        processed = processed.replace("<bLives>", bLives);
        processed = processed.replace("<rLives>", rLives);
        processed = processed.replace("<kills>", String.valueOf(totalKills));
        processed = processed.replace("<goals>", String.valueOf(totalGoals));
        processed = processed.replace("<hits>", String.valueOf(totalHits));
        processed = processed.replace("<your_hits>", String.valueOf(yourHits));
        processed = processed.replace("<opponent_hits>", String.valueOf(opponentHits));
        processed = processed.replace("<combo>", comboText);
        processed = processed.replace("<bRed>", bRed);
        
        // Placeholders específicos do modo SOPA
        if (isSopa) {
            processed = processed.replace("<bSopas>", bSopas);
            processed = processed.replace("<rSopas>", rSopas);
            processed = processed.replace("<streaks>", streaks);
        }
        
        return processed;
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }

    public void removeScoreboard(Player player) {
        if (playerScoreboards.containsKey(player.getUniqueId())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            playerScoreboards.remove(player.getUniqueId());
        }
    }

    public void updateDuelScoreboard(Player player, Duel duel) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;

        Objective objective = scoreboard.getObjective("duel");
        if (objective == null) return;

        // Verificar se existe scoreboard específica para o modo
        List<String> linesToUse = null;
        if (modeDuelLines != null) {
            linesToUse = modeDuelLines.get(duel.getMode());
        }
        
        if (linesToUse == null || linesToUse.isEmpty()) {
            linesToUse = duelLines;
        }

        if (linesToUse != null && !linesToUse.isEmpty()) {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
            objective = scoreboard.registerNewObjective("duel", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(duelTitle != null ? duelTitle : ChatColor.GOLD + "§6§lDUELO");

            Player opponent = Bukkit.getPlayer(duel.getOpponent(player.getUniqueId()));
            if (opponent != null) {
                int score = linesToUse.size();
                for (String line : linesToUse) {
                    String processedLine = processPlaceholders(line, player, opponent, duel);
                    if (processedLine.length() > 40) {
                        processedLine = processedLine.substring(0, 40);
                    }
                    objective.getScore(processedLine).setScore(score);
                    score--;
                }
            }
        } else {
            scoreboard.resetScores(ChatColor.GOLD + "Estado: " + getStateText(Duel.DuelState.COUNTDOWN));
            scoreboard.resetScores(ChatColor.GOLD + "Estado: " + getStateText(Duel.DuelState.FIGHTING));
            scoreboard.resetScores(ChatColor.GOLD + "Estado: " + getStateText(Duel.DuelState.ENDING));

            Score stateScore = objective.getScore(ChatColor.GOLD + "Estado: " + getStateText(duel.getState()));
            stateScore.setScore(2);
        }
    }

    private String getStateText(Duel.DuelState state) {
        switch (state) {
            case COUNTDOWN:
                return ChatColor.YELLOW + "Contagem";
            case FIGHTING:
                return ChatColor.RED + "Lutando";
            case ENDING:
                return ChatColor.GRAY + "Finalizando";
            default:
                return ChatColor.WHITE + "Desconhecido";
        }
    }

    public void clearAllScoreboards() {
        for (UUID playerId : playerScoreboards.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerScoreboards.clear();
    }

    public void reload(FileConfiguration newConfig, PlaceholderManager newPlaceholderManager) {
        this.scoreboardConfig = newConfig;
        this.placeholderManager = newPlaceholderManager;
        loadConfig();
        
        for (UUID playerId : new java.util.HashMap<>(playerScoreboards).keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                if (duelManager != null && duelManager.isInDuel(player)) {
                    Duel duel = duelManager.getPlayerDuel(player);
                    if (duel != null) {
                        Player opponent = Bukkit.getPlayer(duel.getOpponent(player.getUniqueId()));
                        if (opponent != null) {
                            createDuelScoreboardForPlayer(player, opponent, duel);
                        }
                    }
                } else {
                    createLobbyScoreboard(player);
                }
            }
        }
    }

    private DuelManager duelManager;
    
    public void setDuelManager(DuelManager duelManager) {
        this.duelManager = duelManager;
    }
}

