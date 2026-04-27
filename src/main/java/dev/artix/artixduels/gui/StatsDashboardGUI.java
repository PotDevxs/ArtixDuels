package dev.artix.artixduels.gui;

import dev.artix.artixduels.database.IDuelHistoryDAO;
import dev.artix.artixduels.managers.RankingManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.DuelHistory;
import dev.artix.artixduels.models.DuelMode;
import dev.artix.artixduels.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard avançado de estatísticas com gráficos e comparações.
 */
public class StatsDashboardGUI implements Listener {
    private static final String MODE_STATS_TITLE = ChatColor.translateAlternateColorCodes('&', "&6&lModos de duelo");

    private final StatsManager statsManager;
    private final RankingManager rankingManager;
    private final IDuelHistoryDAO historyDAO;

    public StatsDashboardGUI(StatsManager statsManager, RankingManager rankingManager, IDuelHistoryDAO historyDAO) {
        this.statsManager = statsManager;
        this.rankingManager = rankingManager;
        this.historyDAO = historyDAO;
    }

    /**
     * Abre o dashboard principal de estatísticas.
     */
    public void openDashboard(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lDASHBOARD");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);

        // Item: Estatísticas Gerais
        ItemStack generalItem = createGeneralStatsItem(stats, player);
        gui.setItem(10, generalItem);

        // Item: Estatísticas por Modo
        ItemStack modeItem = createModeStatsItem(stats);
        gui.setItem(12, modeItem);

        // Item: Comparação com Outros
        ItemStack compareItem = createCompareItem(stats, player);
        gui.setItem(14, compareItem);

        // Item: Timeline de Duelos
        ItemStack timelineItem = createTimelineItem();
        gui.setItem(16, timelineItem);

        // Item: Progresso de Objetivos
        ItemStack progressItem = createProgressItem(stats);
        gui.setItem(28, progressItem);

        // Item: Gráficos
        ItemStack graphItem = createGraphItem(stats);
        gui.setItem(30, graphItem);

        // Item: Exportar Dados
        ItemStack exportItem = createExportItem();
        gui.setItem(32, exportItem);

        // Item: Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de estatísticas gerais.
     */
    public void openGeneralStats(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lESTATÍSTICAS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);

        // ELO
        ItemStack eloItem = createStatBarItem(Material.GOLD_INGOT, "&6&lELO", stats.getElo(), 2000, 
            "&7Seu ELO atual: &6" + stats.getElo());
        gui.setItem(10, eloItem);

        // Vitórias
        ItemStack winsItem = createStatBarItem(Material.DIAMOND_SWORD, "&a&lVITÓRIAS", stats.getWins(), 1000,
            "&7Total de vitórias: &a" + stats.getWins());
        gui.setItem(12, winsItem);

        // Derrotas
        ItemStack lossesItem = createStatBarItem(Material.IRON_SWORD, "&c&lDERROTAS", stats.getLosses(), 1000,
            "&7Total de derrotas: &c" + stats.getLosses());
        gui.setItem(14, lossesItem);

        // Winrate
        double winrate = stats.getWinRate();
        ItemStack winrateItem = createStatBarItem(Material.EMERALD, "&b&lWINRATE", (int) winrate, 100,
            "&7Taxa de vitória: &b" + String.format("%.2f", winrate) + "%");
        gui.setItem(16, winrateItem);

        // Win Streak
        ItemStack streakItem = createStatBarItem(Material.BLAZE_POWDER, "&e&lWIN STREAK", stats.getWinStreak(), 50,
            "&7Sequência atual: &e" + stats.getWinStreak());
        gui.setItem(28, streakItem);

        // Melhor Streak
        ItemStack bestStreakItem = createStatBarItem(Material.FIREBALL, "&6&lMELHOR STREAK", stats.getBestWinStreak(), 100,
            "&7Melhor sequência: &6" + stats.getBestWinStreak());
        gui.setItem(30, bestStreakItem);

        // XP
        ItemStack xpItem = createStatBarItem(Material.EXP_BOTTLE, "&e&lXP", stats.getXp(), 10000,
            "&7XP total: &e" + stats.getXp());
        gui.setItem(32, xpItem);

        // Level
        ItemStack levelItem = createStatBarItem(Material.ENCHANTED_BOOK, "&b&lNÍVEL", stats.getLevel(), 100,
            "&7Nível atual: &b" + stats.getLevel());
        gui.setItem(34, levelItem);

        // Item: Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de comparação com outros jogadores.
     */
    public void openCompareMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lCOMPARAÇÃO");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats playerStats = statsManager.getPlayerStats(player);
        List<RankingManager.RankingEntry> topPlayers = rankingManager.getEloRanking(10);

        int slot = 0;
        for (RankingManager.RankingEntry entry : topPlayers) {
            if (slot >= 45) break;
            if (entry.getPlayerId().equals(player.getUniqueId())) continue;

            PlayerStats otherStats = entry.getStats();
            ItemStack compareItem = createComparePlayerItem(playerStats, otherStats, entry.getPlayerName(), slot + 1);
            gui.setItem(slot, compareItem);
            slot++;
        }

        // Item: Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de timeline de duelos (histórico persistido).
     */
    public void openTimelineMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lTIMELINE");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<DuelHistory> history = historyDAO != null
            ? historyDAO.getPlayerHistory(player.getUniqueId(), 28)
            : new ArrayList<DuelHistory>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm", new Locale("pt", "BR"));

        if (history.isEmpty()) {
            ItemStack infoItem = new ItemStack(Material.BOOK);
            ItemMeta infoMeta = infoItem.getItemMeta();
            infoMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7Nenhum duelo registrado"));
            List<String> lore = new ArrayList<>();
            lore.add("&8&m                              ");
            lore.add("&7Seu histórico aparecerá aqui");
            lore.add("&7após duelos finalizados.");
            lore.add("&8&m                              ");
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            infoMeta.setLore(coloredLore);
            infoItem.setItemMeta(infoMeta);
            gui.setItem(22, infoItem);
        } else {
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
            for (int i = 0; i < history.size() && i < slots.length; i++) {
                DuelHistory h = history.get(i);
                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                boolean won = h.getWinnerId() != null && h.getWinnerId().equals(player.getUniqueId());
                String vs = h.getPlayer1Id().equals(player.getUniqueId()) ? h.getPlayer2Name() : h.getPlayer1Name();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    (won ? "&aVitória &7vs &f" : "&cDerrota &7vs &f") + vs));
                List<String> lore = new ArrayList<>();
                lore.add("&8&m                              ");
                lore.add("&7Data: &f" + fmt.format(new Date(h.getTimestamp())));
                lore.add("&7Kit: &e" + (h.getKitName() != null ? h.getKitName() : "-"));
                lore.add("&7Arena: &e" + (h.getArenaName() != null ? h.getArenaName() : "-"));
                lore.add("&7Duração: &b" + (h.getDuration() / 1000) + "s");
                lore.add("&8&m                              ");
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
                paper.setItemMeta(meta);
                gui.setItem(slots[i], paper);
            }
        }

        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre estatísticas detalhadas por modo de duelo.
     */
    public void openModeStatsMenu(Player player) {
        String title = MODE_STATS_TITLE;
        String safeTitle = title.length() > 32 ? title.substring(0, 32) : title;
        Inventory gui = Bukkit.createInventory(null, 54, safeTitle);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        DuelMode[] modes = DuelMode.values();
        for (int i = 0; i < modes.length && i < slots.length; i++) {
            DuelMode mode = modes[i];
            PlayerStats.ModeStats ms = stats.getModeStats(mode);
            ItemStack icon = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + mode.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add("&8&m                              ");
            lore.add("&7Vitórias: &a" + ms.getWins());
            lore.add("&7Derrotas: &c" + ms.getLosses());
            lore.add("&7Kills: &e" + ms.getKills());
            int played = ms.getWins() + ms.getLosses();
            double wr = played > 0 ? (100.0 * ms.getWins() / played) : 0;
            lore.add("&7Winrate: &b" + String.format(Locale.US, "%.1f", wr) + "%");
            lore.add("&8&m                              ");
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
            icon.setItemMeta(meta);
            gui.setItem(slots[i], icon);
        }

        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de progresso de objetivos.
     */
    public void openProgressMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lPROGRESSO");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);

        // Objetivo: 100 Vitórias
        int winsProgress = Math.min(stats.getWins(), 100);
        ItemStack winsGoalItem = createProgressItem("&a&l100 VITÓRIAS", winsProgress, 100,
            "&7Progresso: &a" + winsProgress + "&7/&a100");
        gui.setItem(10, winsGoalItem);

        // Objetivo: ELO 1500
        int eloProgress = Math.min(stats.getElo(), 1500);
        ItemStack eloGoalItem = createProgressItem("&6&lELO 1500", eloProgress, 1500,
            "&7Progresso: &6" + eloProgress + "&7/&61500");
        gui.setItem(12, eloGoalItem);

        // Objetivo: Win Streak 10
        int streakProgress = Math.min(stats.getBestWinStreak(), 10);
        ItemStack streakGoalItem = createProgressItem("&e&lSTREAK 10", streakProgress, 10,
            "&7Progresso: &e" + streakProgress + "&7/&e10");
        gui.setItem(14, streakGoalItem);

        // Objetivo: Nível 50
        int levelProgress = Math.min(stats.getLevel(), 50);
        ItemStack levelGoalItem = createProgressItem("&b&lNÍVEL 50", levelProgress, 50,
            "&7Progresso: &b" + levelProgress + "&7/&b50");
        gui.setItem(16, levelGoalItem);

        // Item: Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de gráficos.
     */
    public void openGraphsMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lGRÁFICOS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        PlayerStats stats = statsManager.getPlayerStats(player);

        // Gráfico de ELO ao longo do tempo
        ItemStack eloGraphItem = createGraphBarItem("&6&lELO", stats.getElo(), 2000);
        gui.setItem(10, eloGraphItem);

        // Gráfico de Winrate por Modo
        ItemStack modeGraphItem = createModeGraphItem(stats);
        gui.setItem(12, modeGraphItem);

        // Gráfico de Vitórias vs Derrotas
        ItemStack winsLossesItem = createWinsLossesGraphItem(stats);
        gui.setItem(14, winsLossesItem);

        // Item: Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("DASHBOARD") && !title.contains("ESTATÍSTICAS") && 
            !title.contains("COMPARAÇÃO") && !title.contains("TIMELINE") && 
            !title.contains("PROGRESSO") && !title.contains("GRÁFICOS") &&
            !title.contains("Modos de duelo")) {
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

        if (title.contains("DASHBOARD")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else if (displayName.contains("GERAIS") || displayName.contains("ESTATÍSTICAS")) {
                openGeneralStats(player);
            } else if (displayName.contains("MODO")) {
                openModeStatsMenu(player);
            } else if (displayName.contains("COMPARAÇÃO")) {
                openCompareMenu(player);
            } else if (displayName.contains("TIMELINE")) {
                openTimelineMenu(player);
            } else if (displayName.contains("PROGRESSO")) {
                openProgressMenu(player);
            } else if (displayName.contains("GRÁFICOS")) {
                openGraphsMenu(player);
            } else if (displayName.contains("EXPORTAR")) {
                exportPlayerData(player);
            }
        } else if (displayName.contains("VOLTAR")) {
            openDashboard(player);
        }
    }

    /**
     * Exporta dados do jogador.
     */
    private void exportPlayerData(Player player) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        
        StringBuilder data = new StringBuilder();
        data.append("=== ESTATÍSTICAS DE ").append(player.getName()).append(" ===\n\n");
        data.append("ELO: ").append(stats.getElo()).append("\n");
        data.append("Vitórias: ").append(stats.getWins()).append("\n");
        data.append("Derrotas: ").append(stats.getLosses()).append("\n");
        data.append("Winrate: ").append(String.format("%.2f", stats.getWinRate())).append("%\n");
        data.append("Win Streak: ").append(stats.getWinStreak()).append("\n");
        data.append("Melhor Streak: ").append(stats.getBestWinStreak()).append("\n");
        data.append("XP: ").append(stats.getXp()).append("\n");
        data.append("Nível: ").append(stats.getLevel()).append("\n\n");
        
        data.append("=== ESTATÍSTICAS POR MODO ===\n");
        for (DuelMode mode : DuelMode.values()) {
            PlayerStats.ModeStats modeStats = stats.getModeStats(mode);
            data.append(mode.getDisplayName()).append(":\n");
            data.append("  Vitórias: ").append(modeStats.getWins()).append("\n");
            data.append("  Derrotas: ").append(modeStats.getLosses()).append("\n");
            data.append("  Kills: ").append(modeStats.getKills()).append("\n\n");
        }

        // Enviar para o jogador (em um servidor real, poderia salvar em arquivo)
        player.sendMessage("§6§l=== DADOS EXPORTADOS ===");
        for (String line : data.toString().split("\n")) {
            player.sendMessage("§7" + line);
        }
        player.sendMessage("§6§l========================");
    }

    private ItemStack createGeneralStatsItem(PlayerStats stats, Player player) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lESTATÍSTICAS GERAIS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7ELO: &6" + stats.getElo());
        lore.add("&7Vitórias: &a" + stats.getWins());
        lore.add("&7Derrotas: &c" + stats.getLosses());
        lore.add("&7Winrate: &b" + String.format("%.2f", stats.getWinRate()) + "%");
        lore.add("&7Streak: &e" + stats.getWinStreak());
        lore.add("&7XP: &e" + stats.getXp());
        lore.add("&7Nível: &b" + stats.getLevel());
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver detalhes");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createModeStatsItem(PlayerStats stats) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lESTATÍSTICAS POR MODO"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        int totalModeWins = 0;
        int totalModeKills = 0;
        for (DuelMode mode : DuelMode.values()) {
            PlayerStats.ModeStats modeStats = stats.getModeStats(mode);
            if (modeStats.getWins() > 0 || modeStats.getKills() > 0) {
                totalModeWins += modeStats.getWins();
                totalModeKills += modeStats.getKills();
            }
        }
        lore.add("&7Total de vitórias: &a" + totalModeWins);
        lore.add("&7Total de kills: &e" + totalModeKills);
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver detalhes");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createCompareItem(PlayerStats stats, Player player) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lCOMPARAÇÃO"));

        int playerRank = rankingManager.getPlayerRank(player.getUniqueId(), "elo");
        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Seu ranking: &6#" + (playerRank > 0 ? playerRank : "N/A"));
        lore.add("&8&m                              ");
        lore.add("&7Clique para comparar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createTimelineItem() {
        ItemStack item = new ItemStack(Material.WATCH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lTIMELINE DE DUELOS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Veja seu histórico");
        lore.add("&7de duelos recentes");
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createProgressItem(PlayerStats stats) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lPROGRESSO DE OBJETIVOS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Veja seu progresso");
        lore.add("&7em objetivos");
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGraphItem(PlayerStats stats) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lGRÁFICOS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Visualize gráficos");
        lore.add("&7de suas estatísticas");
        lore.add("&8&m                              ");
        lore.add("&7Clique para ver");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createExportItem() {
        ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lEXPORTAR DADOS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Exporte suas");
        lore.add("&7estatísticas");
        lore.add("&8&m                              ");
        lore.add("&7Clique para exportar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createStatBarItem(Material material, String name, int value, int max, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> loreList = new ArrayList<>();
        loreList.add("&8&m                              ");
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        loreList.add("&8&m                              ");
        
        // Criar barra visual
        int barLength = 20;
        int filled = (int) ((double) value / max * barLength);
        filled = Math.min(filled, barLength);
        filled = Math.max(filled, 0);
        
        StringBuilder bar = new StringBuilder("&7[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("&a█");
            } else {
                bar.append("&7█");
            }
        }
        bar.append("&7] &b").append(String.format("%.1f", (double) value / max * 100)).append("%");
        loreList.add(bar.toString());
        loreList.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : loreList) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createComparePlayerItem(PlayerStats playerStats, PlayerStats otherStats, String otherName, int rank) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6#" + rank + " &e" + otherName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7ELO: &6" + otherStats.getElo() + " &7(vs &6" + playerStats.getElo() + "&7)");
        int eloDiff = otherStats.getElo() - playerStats.getElo();
        lore.add("&7Diferença: " + (eloDiff >= 0 ? "&a+" : "&c") + eloDiff);
        lore.add("&8&m                              ");
        lore.add("&7Vitórias: &a" + otherStats.getWins() + " &7(vs &a" + playerStats.getWins() + "&7)");
        lore.add("&7Winrate: &b" + String.format("%.2f", otherStats.getWinRate()) + "% &7(vs &b" + 
                 String.format("%.2f", playerStats.getWinRate()) + "%&7)");
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createProgressItem(String name, int progress, int max, String... lore) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> loreList = new ArrayList<>();
        loreList.add("&8&m                              ");
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        loreList.add("&8&m                              ");
        
        // Barra de progresso
        int barLength = 20;
        int filled = (int) ((double) progress / max * barLength);
        filled = Math.min(filled, barLength);
        filled = Math.max(filled, 0);
        
        StringBuilder bar = new StringBuilder("&7[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("&a█");
            } else {
                bar.append("&7█");
            }
        }
        bar.append("&7] &b").append(String.format("%.1f", (double) progress / max * 100)).append("%");
        loreList.add(bar.toString());
        
        if (progress >= max) {
            loreList.add("&a&lCONCLUÍDO!");
        } else {
            int remaining = max - progress;
            loreList.add("&7Faltam: &b" + remaining);
        }
        loreList.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : loreList) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGraphBarItem(String name, int value, int max) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Valor: &b" + value);
        lore.add("&8&m                              ");
        
        // Gráfico de barras vertical
        int barHeight = 10;
        int filled = (int) ((double) value / max * barHeight);
        filled = Math.min(filled, barHeight);
        filled = Math.max(filled, 0);
        
        for (int i = barHeight - 1; i >= 0; i--) {
            StringBuilder line = new StringBuilder("&7");
            if (i < filled) {
                line.append("&a█&a█&a█&a█&a█&a█&a█&a█&a█&a█");
            } else {
                line.append("&7█&7█&7█&7█&7█&7█&7█&7█&7█&7█");
            }
            lore.add(line.toString());
        }
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createModeGraphItem(PlayerStats stats) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lWINRATE POR MODO"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        
        for (DuelMode mode : DuelMode.values()) {
            PlayerStats.ModeStats modeStats = stats.getModeStats(mode);
            int total = modeStats.getWins() + modeStats.getLosses();
            if (total > 0) {
                double winrate = (double) modeStats.getWins() / total * 100;
                int barLength = 15;
                int filled = (int) (winrate / 100 * barLength);
                
                StringBuilder bar = new StringBuilder("&7" + mode.getDisplayName() + ": &7[");
                for (int i = 0; i < barLength; i++) {
                    if (i < filled) {
                        bar.append("&a█");
                    } else {
                        bar.append("&7█");
                    }
                }
                bar.append("&7] &b").append(String.format("%.1f", winrate)).append("%");
                lore.add(bar.toString());
            }
        }
        
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createWinsLossesGraphItem(PlayerStats stats) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lVITÓRIAS VS DERROTAS"));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Vitórias: &a" + stats.getWins());
        lore.add("&7Derrotas: &c" + stats.getLosses());
        lore.add("&8&m                              ");
        
        int total = stats.getWins() + stats.getLosses();
        if (total > 0) {
            int winsBar = (int) ((double) stats.getWins() / total * 20);
            int lossesBar = 20 - winsBar;
            
            StringBuilder bar = new StringBuilder("&7[");
            for (int i = 0; i < winsBar; i++) {
                bar.append("&a█");
            }
            for (int i = 0; i < lossesBar; i++) {
                bar.append("&c█");
            }
            bar.append("&7]");
            lore.add(bar.toString());
            StringBuilder percentageBar = new StringBuilder("&a");
            percentageBar.append(String.format("%.1f", (double) stats.getWins() / total * 100));
            percentageBar.append("% &7| &c");
            percentageBar.append(String.format("%.1f", (double) stats.getLosses() / total * 100));
            percentageBar.append("%");
            lore.add(percentageBar.toString());
        }
        
        lore.add("&8&m                              ");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }
}

