package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.ChallengeManager;
import dev.artix.artixduels.models.Challenge;
import dev.artix.artixduels.models.ChallengeProgress;

import java.util.Map;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI para exibir desafios diários e semanais.
 */
public class ChallengeGUI implements Listener {
    private final ChallengeManager challengeManager;

    public ChallengeGUI(ChallengeManager challengeManager) {
        this.challengeManager = challengeManager;
    }

    /**
     * Abre o menu principal de desafios.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lDESAFIOS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        ItemStack dailyItem = createCategoryItem(Material.GOLD_INGOT, "&e&lDESAFIOS DIÁRIOS",
            "&7Veja seus desafios diários",
            "&7Clique para visualizar");
        gui.setItem(20, dailyItem);

        ItemStack weeklyItem = createCategoryItem(Material.DIAMOND, "&b&lDESAFIOS SEMANAIS",
            "&7Veja seus desafios semanais",
            "&7Clique para visualizar");
        gui.setItem(24, weeklyItem);

        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de desafios de um tipo específico.
     */
    public void openChallengesMenu(Player player, Challenge.ChallengeType type) {
        String typeName = type == Challenge.ChallengeType.DAILY ? "DIÁRIOS" : "SEMANAIS";
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lDESAFIOS " + typeName);
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<Challenge> challenges = challengeManager.getActiveChallenges(player.getUniqueId(), type);
        
        int slot = 0;
        for (Challenge challenge : challenges) {
            if (slot >= 45) break;

            ChallengeProgress progress = challengeManager.getProgress(player.getUniqueId(), challenge.getId());
            ItemStack challengeItem = createChallengeItem(challenge, progress, player);
            gui.setItem(slot, challengeItem);
            slot++;
        }

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

        if (!title.contains("DESAFIOS")) {
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

        if (title.equals(ChatColor.translateAlternateColorCodes('&', "&6&lDESAFIOS"))) {
            if (displayName.contains("DIÁRIOS")) {
                openChallengesMenu(player, Challenge.ChallengeType.DAILY);
            } else if (displayName.contains("SEMANAIS")) {
                openChallengesMenu(player, Challenge.ChallengeType.WEEKLY);
            } else if (displayName.contains("FECHAR")) {
                player.closeInventory();
            }
        } else {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            }
        }
    }

    private ItemStack createCategoryItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private ItemStack createChallengeItem(Challenge challenge, ChallengeProgress progress) {
        return createChallengeItem(challenge, progress, null);
    }

    private ItemStack createChallengeItem(Challenge challenge, ChallengeProgress progress, Player player) {
        ItemStack item = new ItemStack(challenge.getDisplayMaterial(), 1, (short) challenge.getDisplayData());
        ItemMeta meta = item.getItemMeta();
        
        String statusColor = progress.isCompleted() ? "&a" : "&7";
        String statusText = progress.isCompleted() ? "&a&lCONCLUÍDO" : "&7Em progresso";
        
        String challengeName = challenge.getName();
        String challengeDescription = challenge.getDescription();
        
        // Processar placeholder <theme> se houver jogador
        if (player != null) {
            try {
                dev.artix.artixduels.managers.ThemeManager themeManager = 
                    ((dev.artix.artixduels.ArtixDuels) org.bukkit.Bukkit.getPluginManager().getPlugin("ArtixDuels")).getThemeManager();
                if (themeManager != null) {
                    challengeName = themeManager.processThemePlaceholder(challengeName, player.getUniqueId());
                    challengeDescription = themeManager.processThemePlaceholder(challengeDescription, player.getUniqueId());
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            statusColor + challengeName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + challengeDescription);
        lore.add("&8&m                              ");
        
        int progressPercent = challenge.getTarget() > 0 ? 
            (progress.getProgress() * 100 / challenge.getTarget()) : 0;
        progressPercent = Math.min(100, Math.max(0, progressPercent));
        
        String progressBar = createProgressBar(progressPercent, 20);
        lore.add("&7Progresso: &b" + progress.getProgress() + "&7/&b" + challenge.getTarget());
        lore.add(progressBar);
        lore.add("&8&m                              ");
        
        if (progress.isCompleted()) {
            lore.add("&a&l✓ Desafio concluído!");
        } else {
            lore.add("&7Recompensas:");
            Map<String, Object> rewards = challenge.getRewards();
            if (rewards.containsKey("money")) {
                double money = ((Number) rewards.get("money")).doubleValue();
                lore.add(" &6" + money + " moedas");
            }
            if (rewards.containsKey("xp")) {
                int xp = ((Number) rewards.get("xp")).intValue();
                lore.add(" &b" + xp + " XP");
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

    private String createProgressBar(int percent, int length) {
        int filled = (percent * length) / 100;
        int empty = length - filled;
        
        StringBuilder bar = new StringBuilder("&7[");
        for (int i = 0; i < filled; i++) {
            bar.append("&a█");
        }
        for (int i = 0; i < empty; i++) {
            bar.append("&7█");
        }
        bar.append("&7] &b").append(percent).append("%");
        
        return bar.toString();
    }
}

