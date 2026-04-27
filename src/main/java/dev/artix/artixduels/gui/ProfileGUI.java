package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.MenuManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI implements Listener {
    private StatsManager statsManager;
    private MenuManager menuManager;

    public ProfileGUI(StatsManager statsManager, MenuManager menuManager) {
        this.statsManager = statsManager;
        this.menuManager = menuManager;
    }

    public void openProfile(Player player) {
        MenuManager.MenuData menuData = menuManager.getMenu("profile");
        String title = menuData != null ? menuData.getTitle() : "&6&lPerfil";
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        int size = menuData != null ? menuData.getSize() : 54;
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Preencher bordas do menu
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);
        
        ItemStack playerHead = createPlayerHead(player, stats);
        gui.setItem(4, playerHead);

        ItemStack eloItem = createEloItem(stats);
        gui.setItem(19, eloItem);

        ItemStack rankItem = createRankItem(stats);
        gui.setItem(21, rankItem);

        ItemStack xpItem = createXPItem(stats);
        gui.setItem(23, xpItem);

        ItemStack winsItem = createStatItem(Material.DIAMOND_SWORD, "&a&lVitórias", "&7Total: &a" + stats.getWins(), "&7Win Rate: &a" + String.format("%.2f", stats.getWinRate()) + "%");
        gui.setItem(28, winsItem);

        ItemStack lossesItem = createStatItem(Material.IRON_SWORD, "&c&lDerrotas", "&7Total: &c" + stats.getLosses());
        gui.setItem(30, lossesItem);

        ItemStack streakItem = createStatItem(Material.FIREBALL, "&e&lSequência", "&7Atual: &e" + stats.getWinStreak(), "&7Melhor: &e" + stats.getBestWinStreak());
        gui.setItem(32, streakItem);

        ItemStack killsItem = createKillsItem(stats);
        gui.setItem(34, killsItem);

        int slot = 36;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;
            ItemStack modeItem = createModeStatItem(mode, stats);
            gui.setItem(slot, modeItem);
            slot++;
        }

        // Adicionar itens do menu configurável
        if (menuData != null) {
            for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                ItemStack item = menuManager.createMenuItem("profile", itemData.getName());
                if (item != null) {
                    gui.setItem(itemData.getSlot(), item);
                }
            }
        } else {
            // Fallback
            ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta closeMeta = closeItem.getItemMeta();
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&l✖ &c&lFechar"));
            closeMeta.setLore(java.util.Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7"), 
                ChatColor.translateAlternateColorCodes('&', "&7Clique para fechar o menu"), 
                ChatColor.translateAlternateColorCodes('&', "&7")));
            closeItem.setItemMeta(closeMeta);
            gui.setItem(49, closeItem);
        }

        player.openInventory(gui);
    }

    private ItemStack createPlayerHead(Player player, PlayerStats stats) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + player.getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7ELO: &e" + stats.getElo()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Rank: &e" + getRankName(stats.getElo())));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7XP: &b" + stats.getXp() + " &7/ &b" + getXpForNextLevel(stats.getLevel())));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Level: &b" + stats.getLevel()));
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createEloItem(PlayerStats stats) {
        int currentElo = stats.getElo();
        RankInfo rankInfo = getRankInfo(currentElo);
        RankInfo nextRank = getNextRank(currentElo);
        
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lELO"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Atual: &e" + currentElo));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Rank: &e" + rankInfo.name));
        if (nextRank != null) {
            int eloProgress = currentElo - rankInfo.minElo;
            int eloNeeded = nextRank.minElo - rankInfo.minElo;
            double progressPercent = eloNeeded > 0 ? (double) eloProgress / eloNeeded * 100 : 100;
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Próximo: &e" + nextRank.name + " &7(" + nextRank.minElo + ")"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Progresso: &a" + String.format("%.1f", progressPercent) + "%"));
            lore.add(createProgressBar(progressPercent));
        } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Você está no rank máximo!"));
            lore.add(createProgressBar(100));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createRankItem(PlayerStats stats) {
        int currentElo = stats.getElo();
        RankInfo rankInfo = getRankInfo(currentElo);
        RankInfo nextRank = getNextRank(currentElo);
        
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lRank"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Atual: &e" + rankInfo.name));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7ELO: &e" + currentElo));
        if (nextRank != null) {
            int eloNeeded = nextRank.minElo - currentElo;
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Próximo: &e" + nextRank.name));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Faltam: &e" + eloNeeded + " ELO"));
        } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Você está no rank máximo!"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createXPItem(PlayerStats stats) {
        int currentXp = stats.getXp();
        int currentLevel = stats.getLevel();
        int xpForNext = getXpForNextLevel(currentLevel);
        int xpProgress = currentXp - getXpForLevel(currentLevel);
        int xpNeeded = xpForNext - getXpForLevel(currentLevel);
        double progressPercent = xpNeeded > 0 ? (double) xpProgress / xpNeeded * 100 : 100;
        
        ItemStack item = new ItemStack(Material.EXP_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lXP & Experiência"));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Level: &b" + currentLevel));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7XP: &b" + currentXp + " &7/ &b" + xpForNext));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Progresso: &a" + String.format("%.1f", progressPercent) + "%"));
        lore.add(createProgressBar(progressPercent));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createKillsItem(PlayerStats stats) {
        int totalKills = 0;
        for (DuelMode mode : DuelMode.values()) {
            totalKills += stats.getModeStats(mode).getKills();
        }
        
        return createStatItem(Material.BOW, "&c&lAbates", "&7Total: &c" + totalKills);
    }

    private ItemStack createModeStatItem(DuelMode mode, PlayerStats stats) {
        PlayerStats.ModeStats modeStats = stats.getModeStats(mode);
        Material material = getMaterialForMode(mode);
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + mode.getDisplayName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Vitórias: &a" + modeStats.getWins()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Derrotas: &c" + modeStats.getLosses()));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Abates: &e" + modeStats.getKills()));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            if (!line.isEmpty()) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private Material getMaterialForMode(DuelMode mode) {
        switch (mode) {
            case BEDFIGHT:
                return Material.BED;
            case STICKFIGHT:
                return Material.STICK;
            case SOUP:
            case SOUPRECRAFT:
                return Material.MUSHROOM_SOUP;
            case GLADIATOR:
                return Material.IRON_SWORD;
            case FASTOB:
                return Material.DIAMOND_SWORD;
            case BOXING:
                return Material.LEATHER_HELMET;
            case FIREBALLFIGHT:
                return Material.FIREBALL;
            case SUMO:
                return Material.SLIME_BALL;
            case BATTLERUSH:
                return Material.BLAZE_POWDER;
            case TNTSUMO:
                return Material.TNT;
            default:
                return Material.BOOK;
        }
    }

    private String createProgressBar(double percent) {
        int filled = (int) (percent / 10);
        int empty = 10 - filled;
        StringBuilder filledBar = new StringBuilder();
        StringBuilder emptyBar = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            filledBar.append("█");
        }
        for (int i = 0; i < empty; i++) {
            emptyBar.append("█");
        }
        return ChatColor.translateAlternateColorCodes('&', "&a" + filledBar.toString() + "&7" + emptyBar.toString());
    }

    private RankInfo getRankInfo(int elo) {
        if (elo >= 2000) return new RankInfo("&c&lMESTRE", 2000);
        if (elo >= 1800) return new RankInfo("&6&lDIAMANTE", 1800);
        if (elo >= 1600) return new RankInfo("&b&lPLATINA", 1600);
        if (elo >= 1400) return new RankInfo("&e&lOURO", 1400);
        if (elo >= 1200) return new RankInfo("&7&lPRATA", 1200);
        if (elo >= 1000) return new RankInfo("&f&lBRONZE", 1000);
        return new RankInfo("&8&lFERRO", 0);
    }

    private RankInfo getNextRank(int elo) {
        if (elo >= 2000) return null;
        if (elo >= 1800) return new RankInfo("&c&lMESTRE", 2000);
        if (elo >= 1600) return new RankInfo("&6&lDIAMANTE", 1800);
        if (elo >= 1400) return new RankInfo("&b&lPLATINA", 1600);
        if (elo >= 1200) return new RankInfo("&e&lOURO", 1400);
        if (elo >= 1000) return new RankInfo("&7&lPRATA", 1200);
        return new RankInfo("&f&lBRONZE", 1000);
    }

    private String getRankName(int elo) {
        return ChatColor.translateAlternateColorCodes('&', getRankInfo(elo).name);
    }

    private int getXpForLevel(int level) {
        return (level - 1) * 100;
    }

    private int getXpForNextLevel(int level) {
        return level * 100;
    }

    private static class RankInfo {
        String name;
        int minElo;

        RankInfo(String name, int minElo) {
            this.name = name;
            this.minElo = minElo;
        }
    }

    /**
     * menus.yml: "PERFIL" em caps — não usar .contains("Perfil") no título com cores.
     */
    private static boolean isProfileMenuTitle(String viewTitle) {
        String t = ChatColor.stripColor(viewTitle).toLowerCase();
        return t.contains("perfil") || t.contains("profile") || t.contains("estat");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        if (!isProfileMenuTitle(event.getView().getTitle())) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        
        if (displayName.contains("Fechar")) {
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onMenuInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isProfileMenuTitle(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
    }
}

