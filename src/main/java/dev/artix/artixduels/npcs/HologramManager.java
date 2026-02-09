package dev.artix.artixduels.npcs;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.PlaceholderManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    private ArtixDuels plugin;
    private DuelManager duelManager;
    private StatsManager statsManager;
    private PlaceholderManager placeholderManager;
    private Map<String, List<ArmorStand>> holograms;
    private Map<String, List<String>> hologramLines;
    private Map<String, DuelMode> hologramModes;
    private int updateTaskId;

    public HologramManager(ArtixDuels plugin, DuelManager duelManager, StatsManager statsManager, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.statsManager = statsManager;
        this.placeholderManager = placeholderManager;
        this.holograms = new HashMap<>();
        this.hologramLines = new HashMap<>();
        this.hologramModes = new HashMap<>();
    }

    public void loadHolograms(FileConfiguration config, String npcName, Location npcLocation, DuelMode mode) {
        ConfigurationSection npcSection = config.getConfigurationSection("npcs." + npcName);
        if (npcSection == null) return;

        ConfigurationSection hologramSection = npcSection.getConfigurationSection("hologram");
        if (hologramSection == null) return;

        boolean enabled = hologramSection.getBoolean("enabled", false);
        if (!enabled) return;

        double height = hologramSection.getDouble("height", 2.5);
        double offsetX = hologramSection.getDouble("offset-x", 0.0);
        double offsetZ = hologramSection.getDouble("offset-z", 0.0);
        List<String> lines = hologramSection.getStringList("lines");

        if (lines.isEmpty()) {
            lines = getDefaultLines();
        }

        hologramLines.put(npcName, lines);
        hologramModes.put(npcName, mode);
        createHologram(npcName, npcLocation, height, offsetX, offsetZ, lines);
    }

    private void createHologram(String npcName, Location baseLocation, double height, double offsetX, double offsetZ, List<String> lines) {
        Location hologramLocation = baseLocation.clone().add(offsetX, height, offsetZ);
        List<ArmorStand> stands = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Location lineLocation = hologramLocation.clone().subtract(0, i * 0.25, 0);

            ArmorStand stand = (ArmorStand) baseLocation.getWorld().spawnEntity(lineLocation, EntityType.ARMOR_STAND);
            stand.setCustomNameVisible(true);
            DuelMode defaultMode = DuelMode.BEDFIGHT;
            stand.setCustomName(processPlaceholders(line, defaultMode));
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setMarker(true);
            stand.setCanPickupItems(false);
            stand.setRemoveWhenFarAway(false);

            stands.add(stand);
        }

        holograms.put(npcName, stands);
    }

    public void updateHolograms() {
        for (Map.Entry<String, List<ArmorStand>> entry : holograms.entrySet()) {
            String npcName = entry.getKey();
            List<ArmorStand> stands = entry.getValue();
            List<String> lines = hologramLines.get(npcName);

            if (lines == null || lines.size() != stands.size()) continue;

            DuelMode mode = hologramModes.get(npcName);
            for (int i = 0; i < stands.size() && i < lines.size(); i++) {
                ArmorStand stand = stands.get(i);
                String line = processPlaceholders(lines.get(i), mode);
                stand.setCustomName(line);
            }
        }
    }

    private String processPlaceholders(String line, DuelMode mode) {
        String processed = ChatColor.translateAlternateColorCodes('&', line);
        
        // Processar placeholder <theme> - usar tema padrão já que não há jogador específico
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = plugin.getThemeManager();
            if (themeManager != null) {
                // Usar tema padrão (dark) para hologramas globais
                String themeColor = themeManager.getTheme("dark").getColor("primary");
                processed = processed.replace("<theme>", themeColor);
            } else {
                processed = processed.replace("<theme>", "&b");
            }
        } catch (Exception e) {
            processed = processed.replace("<theme>", "&b");
        }
        
        if (placeholderManager != null) {
            processed = placeholderManager.processPlaceholders(processed, null, mode);
        } else {
            // Fallback para compatibilidade
            int playersInDuel = duelManager.getActiveDuelsCountByMode(mode) * 2;
            int playersInQueue = duelManager.getMatchmakingQueueSizeByMode(mode);
            
            processed = processed.replace("{players-in-duel}", String.valueOf(playersInDuel));
            processed = processed.replace("{players-in-queue}", String.valueOf(playersInQueue));
            processed = processed.replace("{total-players}", String.valueOf(playersInDuel + playersInQueue));
            processed = processed.replace("{active-duels}", String.valueOf(duelManager.getActiveDuelsCountByMode(mode)));
            processed = processed.replace("{online-players}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        }
        
        return processed;
    }

    private List<String> getDefaultLines() {
        List<String> defaultLines = new ArrayList<>();
        defaultLines.add("&6&lDUELOS");
        defaultLines.add("&7Jogadores em Duelo: &a{players-in-duel}");
        defaultLines.add("&7Jogadores na Fila: &e{players-in-queue}");
        defaultLines.add("&7Duelos Ativos: &b{active-duels}");
        defaultLines.add(" ");
        defaultLines.add("&eClique aqui!");
        return defaultLines;
    }

    public void removeHologram(String npcName) {
        List<ArmorStand> stands = holograms.remove(npcName);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
        hologramLines.remove(npcName);
        hologramModes.remove(npcName);
    }

    public void removeAllHolograms() {
        for (List<ArmorStand> stands : holograms.values()) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
        holograms.clear();
        hologramLines.clear();
        hologramModes.clear();
    }

    public void startUpdateTask() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
        }
        updateTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            updateHolograms();
        }, 0L, 20L);
    }

    public void stopUpdateTask() {
        if (updateTaskId != 0) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
            updateTaskId = 0;
        }
    }
}

