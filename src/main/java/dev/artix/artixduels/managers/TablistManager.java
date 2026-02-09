package dev.artix.artixduels.managers;

import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TablistManager {
    private FileConfiguration tablistConfig;
    private StatsManager statsManager;
    private DuelManager duelManager;
    private Map<UUID, Scoreboard> playerScoreboards;
    private boolean enabled;
    private int updateInterval;
    private boolean headerEnabled;
    private List<String> headerLines;
    private boolean footerEnabled;
    private List<String> footerLines;
    private String playerNameFormat;
    private String sortBy;
    private boolean playerListEnabled;
    private boolean showInDuel;

    public TablistManager(FileConfiguration tablistConfig, StatsManager statsManager, DuelManager duelManager) {
        this.tablistConfig = tablistConfig;
        this.statsManager = statsManager;
        this.duelManager = duelManager;
        this.playerScoreboards = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection tablistSection = tablistConfig.getConfigurationSection("tablist");
        if (tablistSection != null) {
            enabled = tablistSection.getBoolean("enabled", true);
            updateInterval = tablistSection.getInt("update-interval", 20);
        }

        ConfigurationSection headerSection = tablistConfig.getConfigurationSection("header");
        if (headerSection != null) {
            headerEnabled = headerSection.getBoolean("enabled", true);
            headerLines = headerSection.getStringList("lines");
        }

        ConfigurationSection footerSection = tablistConfig.getConfigurationSection("footer");
        if (footerSection != null) {
            footerEnabled = footerSection.getBoolean("enabled", true);
            footerLines = footerSection.getStringList("lines");
        }

        ConfigurationSection playerNameSection = tablistConfig.getConfigurationSection("player-name");
        if (playerNameSection != null) {
            playerNameFormat = playerNameSection.getString("format", "&a{player}");
            sortBy = playerNameSection.getString("sort-by", "elo");
        }

        ConfigurationSection playerListSection = tablistConfig.getConfigurationSection("player-list");
        if (playerListSection != null) {
            playerListEnabled = playerListSection.getBoolean("enabled", true);
            showInDuel = playerListSection.getBoolean("show-in-duel", false);
        }
    }

    public void updateTablist(Player player) {
        if (!enabled) return;

        String header = "";
        String footer = "";

        if (headerEnabled && headerLines != null && !headerLines.isEmpty()) {
            List<String> processedHeader = processHeaderFooter(headerLines, player);
            StringBuilder headerBuilder = new StringBuilder();
            for (int i = 0; i < processedHeader.size(); i++) {
                if (i > 0) headerBuilder.append("\n");
                headerBuilder.append(processedHeader.get(i));
            }
            header = ChatColor.translateAlternateColorCodes('&', headerBuilder.toString());
        }

        if (footerEnabled && footerLines != null && !footerLines.isEmpty()) {
            List<String> processedFooter = processHeaderFooter(footerLines, player);
            StringBuilder footerBuilder = new StringBuilder();
            for (int i = 0; i < processedFooter.size(); i++) {
                if (i > 0) footerBuilder.append("\n");
                footerBuilder.append(processedFooter.get(i));
            }
            footer = ChatColor.translateAlternateColorCodes('&', footerBuilder.toString());
        }

        // Enviar header e footer juntos no mesmo packet
        setPlayerListHeaderFooter(player, header, footer);

        updatePlayerName(player);
    }

    private List<String> processHeaderFooter(List<String> lines, Player player) {
        List<String> processed = new java.util.ArrayList<>();
        for (String line : lines) {
            String processedLine = ChatColor.translateAlternateColorCodes('&', line);
            
            // Processar placeholder <theme> - cor primária do tema do jogador
            try {
                dev.artix.artixduels.managers.ThemeManager themeManager = 
                    ((dev.artix.artixduels.ArtixDuels) org.bukkit.Bukkit.getPluginManager().getPlugin("ArtixDuels")).getThemeManager();
                if (themeManager != null) {
                    String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                    processedLine = processedLine.replace("<theme>", themeColor);
                } else {
                    processedLine = processedLine.replace("<theme>", "&f");
                }
            } catch (Exception e) {
                processedLine = processedLine.replace("<theme>", "&f");
            }
            
            processedLine = processedLine.replace("{player}", player.getName());
            processedLine = processedLine.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
            processedLine = processedLine.replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
            
            PlayerStats stats = statsManager.getPlayerStats(player);
            processedLine = processedLine.replace("{elo}", String.valueOf(stats.getElo()));
            processedLine = processedLine.replace("{wins}", String.valueOf(stats.getWins()));
            processedLine = processedLine.replace("{losses}", String.valueOf(stats.getLosses()));
            processedLine = processedLine.replace("{winrate}", String.format("%.2f", stats.getWinRate()));
            processedLine = processedLine.replace("{ping}", String.valueOf(getPing(player)));
            
            processed.add(processedLine);
        }
        return processed;
    }

    private void updatePlayerName(Player player) {
        if (playerNameFormat == null || playerNameFormat.isEmpty()) return;

        String formattedName = processPlayerNameFormat(player);
        if (formattedName.length() > 16) {
            formattedName = formattedName.substring(0, 16);
        }

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        Team team = scoreboard.getTeam("tab_" + player.getName());
        if (team == null) {
            team = scoreboard.registerNewTeam("tab_" + player.getName());
        }

        team.setPrefix(formattedName);
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private String processPlayerNameFormat(Player player) {
        String formatted = ChatColor.translateAlternateColorCodes('&', playerNameFormat);
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = 
                ((dev.artix.artixduels.ArtixDuels) org.bukkit.Bukkit.getPluginManager().getPlugin("ArtixDuels")).getThemeManager();
            if (themeManager != null) {
                String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                formatted = formatted.replace("<theme>", themeColor);
            } else {
                formatted = formatted.replace("<theme>", "&f");
            }
        } catch (Exception e) {
            formatted = formatted.replace("<theme>", "&f");
        }
        
        formatted = formatted.replace("{player}", player.getName());
        formatted = formatted.replace("{ping}", String.valueOf(getPing(player)));
        
        PlayerStats stats = statsManager.getPlayerStats(player);
        formatted = formatted.replace("{elo}", String.valueOf(stats.getElo()));
        formatted = formatted.replace("{wins}", String.valueOf(stats.getWins()));
        formatted = formatted.replace("{losses}", String.valueOf(stats.getLosses()));
        formatted = formatted.replace("{winrate}", String.format("%.2f", stats.getWinRate()));
        
        Duel duel = duelManager.getPlayerDuel(player);
        if (duel != null) {
            formatted = formatted.replace("{duel}", "Em Duelo");
        } else {
            formatted = formatted.replace("{duel}", "");
        }
        
        return formatted;
    }

    public void updateAllTablists() {
        if (!enabled) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (duelManager.isInDuel(player) && !showInDuel) {
                continue;
            }
            updateTablist(player);
        }
    }

    public void removePlayerTablist(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            Team team = scoreboard.getTeam("tab_" + player.getName());
            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }
        }
        playerScoreboards.remove(player.getUniqueId());
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Define o header e footer do tablist para um jogador.
     * O packet PacketPlayOutPlayerListHeaderFooter requer ambos header e footer.
     * Compatível com Minecraft 1.8.
     */
    private void setPlayerListHeaderFooter(Player player, String header, String footer) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            
            // Escapar caracteres especiais para JSON
            String escapedHeader = header.replace("\\", "\\\\")
                                         .replace("\"", "\\\"")
                                         .replace("\n", "\\n");
            String escapedFooter = footer.replace("\\", "\\\\")
                                         .replace("\"", "\\\"")
                                         .replace("\n", "\\n");
            
            Class<?> packetClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutPlayerListHeaderFooter");
            Object packet = packetClass.newInstance();
            
            // No Minecraft 1.8, ChatSerializer é uma inner class de IChatBaseComponent
            // Usar o nome completo com $ para inner classes
            Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
            
            // Definir header
            Object headerComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + escapedHeader + "\"}");
            java.lang.reflect.Field a = packetClass.getDeclaredField("a");
            a.setAccessible(true);
            a.set(packet, headerComponent);
            
            // Definir footer
            Object footerComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "{\"text\":\"" + escapedFooter + "\"}");
            java.lang.reflect.Field b = packetClass.getDeclaredField("b");
            b.setAccessible(true);
            b.set(packet, footerComponent);
            
            // Enviar packet
            Object connection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
            connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet")).invoke(connection, packet);
        } catch (Exception e) {
            // Silenciar erros para evitar spam no console
            // O tablist pode não funcionar em algumas versões, mas não deve quebrar o plugin
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}

