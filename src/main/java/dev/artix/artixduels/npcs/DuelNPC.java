package dev.artix.artixduels.npcs;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.PlaceholderManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.DuelMode;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelNPC {
    private ArtixDuels plugin;
    private Map<String, NPC> npcs;
    private Map<String, DuelMode> npcModes;
    private DuelManager duelManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private PlaceholderManager placeholderManager;
    private HologramManager hologramManager;

    public DuelNPC(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, StatsManager statsManager, PlaceholderManager placeholderManager) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.placeholderManager = placeholderManager;
        this.npcs = new HashMap<>();
        this.npcModes = new HashMap<>();
        this.hologramManager = new HologramManager(plugin, duelManager, statsManager, placeholderManager);
    }

    public void loadNPCs(FileConfiguration config) {
        if (!CitizensAPI.hasImplementation()) {
            plugin.getLogger().warning("Citizens não está instalado! NPCs não serão criados.");
            return;
        }

        ConfigurationSection npcsMainSection = config.getConfigurationSection("npcs");
        if (npcsMainSection == null) return;

        boolean enabled = npcsMainSection.getBoolean("enabled", true);
        if (!enabled) {
            plugin.getLogger().info("NPCs estão desabilitados no npcs.yml");
            return;
        }

        for (String npcName : npcsMainSection.getKeys(false)) {
            if (npcName.equals("enabled")) continue;
            
            ConfigurationSection npcSection = npcsMainSection.getConfigurationSection(npcName);
            if (npcSection == null) continue;

            String modeString = npcSection.getString("mode", "");
            DuelMode mode = DuelMode.fromString(modeString);
            if (mode == null) {
                plugin.getLogger().warning("Modo inválido para NPC: " + npcName + " (modo: " + modeString + ")");
                continue;
            }

            String displayNameRaw = npcSection.getString("display-name", npcName);
            // Processar placeholder <theme> no display-name
            String displayName = processThemePlaceholder(displayNameRaw);
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            String skinName = npcSection.getString("skin", "");
            Location location = parseLocation(npcSection.getString("location", ""));

            if (location == null) {
                plugin.getLogger().warning("Localização inválida para NPC: " + npcName);
                continue;
            }

            boolean lookCloseEnabled = true;
            int lookCloseRange = 5;
            ConfigurationSection lookCloseSection = npcSection.getConfigurationSection("look-close");
            if (lookCloseSection != null) {
                lookCloseEnabled = lookCloseSection.getBoolean("enabled", true);
                lookCloseRange = lookCloseSection.getInt("range", 5);
            }

            createNPC(npcName, displayName, skinName, location, lookCloseEnabled, lookCloseRange, mode, npcSection);
            hologramManager.loadHolograms(config, npcName, location, mode);
            npcModes.put(npcName, mode);
        }
        
        hologramManager.startUpdateTask();
    }

    private void createNPC(String name, String displayName, String skinName, Location location, boolean lookCloseEnabled, int lookCloseRange, DuelMode mode, ConfigurationSection npcSection) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, displayName);
        npc.spawn(location);

        if (!skinName.isEmpty()) {
            SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
            skinTrait.setSkinName(skinName);
        }

        LookClose lookClose = npc.getTrait(LookClose.class);
        lookClose.setRange(lookCloseRange);
        lookClose.lookClose(lookCloseEnabled);

        Equipment equipment = npc.getTrait(Equipment.class);
        
        // Carregar equipment do config
        ConfigurationSection equipmentSection = npcSection.getConfigurationSection("equipment");
        if (equipmentSection != null) {
            // Hand
            if (equipmentSection.contains("hand")) {
                String handItem = equipmentSection.getString("hand");
                if (handItem != null && !handItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.HAND, parseItemStack(handItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.HAND, null);
                }
            } else {
                equipment.set(Equipment.EquipmentSlot.HAND, null);
            }
            
            // Off-hand
            if (equipmentSection.contains("off-hand")) {
                String offHandItem = equipmentSection.getString("off-hand");
                if (offHandItem != null && !offHandItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.OFF_HAND, parseItemStack(offHandItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.OFF_HAND, null);
                }
            } else {
                equipment.set(Equipment.EquipmentSlot.OFF_HAND, null);
            }
            
            // Helmet
            if (equipmentSection.contains("helmet")) {
                String helmetItem = equipmentSection.getString("helmet");
                if (helmetItem != null && !helmetItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.HELMET, parseItemStack(helmetItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.HELMET, null);
                }
            }
            
            // Chestplate
            if (equipmentSection.contains("chestplate")) {
                String chestplateItem = equipmentSection.getString("chestplate");
                if (chestplateItem != null && !chestplateItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.CHESTPLATE, parseItemStack(chestplateItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.CHESTPLATE, null);
                }
            }
            
            // Leggings
            if (equipmentSection.contains("leggings")) {
                String leggingsItem = equipmentSection.getString("leggings");
                if (leggingsItem != null && !leggingsItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.LEGGINGS, parseItemStack(leggingsItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.LEGGINGS, null);
                }
            }
            
            // Boots
            if (equipmentSection.contains("boots")) {
                String bootsItem = equipmentSection.getString("boots");
                if (bootsItem != null && !bootsItem.equalsIgnoreCase("null")) {
                    equipment.set(Equipment.EquipmentSlot.BOOTS, parseItemStack(bootsItem));
                } else {
                    equipment.set(Equipment.EquipmentSlot.BOOTS, null);
                }
            }
        } else {
            // Default: sem equipment
            equipment.set(Equipment.EquipmentSlot.HAND, null);
            equipment.set(Equipment.EquipmentSlot.OFF_HAND, null);
        }

        npc.data().set("duel-npc", true);
        npc.data().set("duel-npc-name", name);
        npc.data().set("duel-mode", mode.getName());

        npcs.put(name, npc);
        plugin.getLogger().info("NPC criado: " + name + " (Modo: " + mode.getDisplayName() + ") em " + location.toString());
    }

    private org.bukkit.inventory.ItemStack parseItemStack(String itemString) {
        if (itemString == null || itemString.isEmpty() || itemString.equalsIgnoreCase("null")) {
            return null;
        }

        try {
            String[] parts = itemString.split(":");
            String materialName = parts[0].toUpperCase();
            org.bukkit.Material material = org.bukkit.Material.valueOf(materialName);
            
            short data = 0;
            if (parts.length > 1) {
                data = Short.parseShort(parts[1]);
            }
            
            return new org.bukkit.inventory.ItemStack(material, 1, data);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao parsear item: " + itemString + " - " + e.getMessage());
            return null;
        }
    }

    public void onNPCClick(Player player, NPC npc) {
        if (!npc.data().has("duel-npc") || !npc.data().get("duel-npc").equals(true)) {
            return;
        }

        String npcName = npc.data().get("duel-npc-name").toString();
        openDuelGUI(player, npcName);
    }

    private void openDuelGUI(Player player, String npcName) {
        DuelMode mode = npcModes.get(npcName);
        if (mode == null) {
            player.sendMessage("§cErro ao identificar o modo de duelo!");
            return;
        }

        // Adicionar diretamente à fila de matchmaking
        if (duelManager.isInDuel(player)) {
            player.sendMessage("§cVocê já está em um duelo!");
            return;
        }

        if (duelManager.isInQueue(player)) {
            player.sendMessage("§cVocê já está na fila de matchmaking!");
            return;
        }

        duelManager.addToMatchmaking(player, mode);
        player.sendMessage("§aVocê entrou na fila de matchmaking para §e" + mode.getDisplayName() + "§a!");
    }

    public DuelMode getNPCMode(String npcName) {
        return npcModes.get(npcName);
    }

    private String processThemePlaceholder(String text) {
        if (text == null) return text;
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = plugin.getThemeManager();
            if (themeManager != null) {
                // Usar tema padrão (dark) para NPCs globais
                String themeColor = themeManager.getTheme("dark").getColor("primary");
                return text.replace("<theme>", themeColor);
            }
        } catch (Exception e) {
            // Ignorar erros
        }
        return text.replace("<theme>", "&b");
    }

    private Location parseLocation(String locString) {
        if (locString.isEmpty()) return null;

        String[] parts = locString.split(",");
        if (parts.length < 4) return null;

        try {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(org.bukkit.Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao parsear localização: " + locString);
            return null;
        }
    }

    public void removeAllNPCs() {
        hologramManager.stopUpdateTask();
        hologramManager.removeAllHolograms();
        
        for (NPC npc : npcs.values()) {
            npc.destroy();
        }
        npcs.clear();
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public NPC getNPC(String name) {
        return npcs.get(name);
    }
}

