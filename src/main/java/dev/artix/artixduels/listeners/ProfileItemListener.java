package dev.artix.artixduels.listeners;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.DuelModeSelectionGUI;
import dev.artix.artixduels.gui.ProfileGUI;
import dev.artix.artixduels.gui.ScoreboardModeSelectionGUI;
import dev.artix.artixduels.managers.DuelManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProfileItemListener implements Listener {
    private DuelModeSelectionGUI duelModeSelectionGUI;
    private ProfileGUI profileGUI;
    private DuelManager duelManager;
    private static final int PROFILE_ITEM_SLOT = 4;
    private static final int QUEUE_ITEM_SLOT = 0;
    private static final int CHALLENGE_ITEM_SLOT = 8;

    public ProfileItemListener(ArtixDuels plugin, ScoreboardModeSelectionGUI scoreboardModeSelectionGUI) {
    }

    public void setDuelModeSelectionGUI(DuelModeSelectionGUI duelModeSelectionGUI) {
        this.duelModeSelectionGUI = duelModeSelectionGUI;
    }

    public void setProfileGUI(ProfileGUI profileGUI) {
        this.profileGUI = profileGUI;
    }

    public void setDuelManager(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveHotbarItems(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String displayName = meta.getDisplayName();
        
        if (displayName.contains("Perfil") || displayName.contains("Profile")) {
            event.setCancelled(true);
            if (profileGUI != null) {
                profileGUI.openProfile(player);
            }
        } else if (displayName.contains("Procurar Partida") || displayName.contains("Queue")) {
            event.setCancelled(true);
            if (duelModeSelectionGUI != null) {
                duelModeSelectionGUI.openQueueMenu(player);
            }
        } else if (displayName.contains("Desafiar") || displayName.contains("Challenge")) {
            event.setCancelled(true);
            player.sendMessage("§7Clique em um jogador para desafiar!");
        } else if (displayName.contains("Sair da Fila") || displayName.contains("Leave Queue")) {
            event.setCancelled(true);
            if (duelManager != null) {
                duelManager.removeFromMatchmaking(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = meta.getDisplayName();
        if (displayName.contains("Desafiar") || displayName.contains("Challenge")) {
            event.setCancelled(true);
            if (duelModeSelectionGUI != null) {
                duelModeSelectionGUI.openModeSelectionMenu(player, target.getName());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = meta.getDisplayName();
        if (displayName.contains("Desafiar") || displayName.contains("Challenge")) {
            event.setCancelled(true);
            if (duelModeSelectionGUI != null) {
                duelModeSelectionGUI.openModeSelectionMenu(player, target.getName());
            }
        }
    }

    public void giveHotbarItems(Player player) {
        if (duelManager != null && duelManager.isInDuel(player)) {
            return;
        }

        player.getInventory().setItem(PROFILE_ITEM_SLOT, createProfileItem());
        player.getInventory().setItem(QUEUE_ITEM_SLOT, createQueueItem());
        player.getInventory().setItem(CHALLENGE_ITEM_SLOT, createChallengeItem());
    }
    
    public static ItemStack createProfileItem() {
        ItemStack profileItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = profileItem.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lPerfil"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Veja suas estatísticas"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7ELO, XP, Rank e muito mais"));
        meta.setLore(lore);
        
        profileItem.setItemMeta(meta);
        return profileItem;
    }

    public static ItemStack createQueueItem() {
        ItemStack queueItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = queueItem.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lProcurar Partida"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique para procurar"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7uma partida"));
        meta.setLore(lore);
        
        queueItem.setItemMeta(meta);
        return queueItem;
    }

    public static ItemStack createChallengeItem() {
        ItemStack challengeItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = challengeItem.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lDesafiar Player"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique em um jogador"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7para desafiar"));
        meta.setLore(lore);
        
        challengeItem.setItemMeta(meta);
        return challengeItem;
    }

    public static void createLeaveQueueItem(Player player) {
        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = leaveItem.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lSair da Fila"));
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Clique para sair"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7da fila de matchmaking"));
        meta.setLore(lore);
        
        leaveItem.setItemMeta(meta);
        player.getInventory().setItem(4, leaveItem);
    }

    /**
     * Verifica se o item é um dos itens fixos do menu (não podem ser movidos ou dropados).
     */
    public static boolean isFixedMenuItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String name = ChatColor.stripColor(meta.getDisplayName());
        return name.contains("Procurar Partida") || name.contains("Queue")
            || name.contains("Perfil") || name.contains("Profile")
            || name.contains("Desafiar") || name.contains("Challenge")
            || name.contains("Sair da Fila") || name.contains("Leave Queue");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isFixedMenuItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (isFixedMenuItem(event.getCurrentItem()) || isFixedMenuItem(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (isFixedMenuItem(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= topSize) {
                int bottomSlot = rawSlot - topSize;
                if (bottomSlot == QUEUE_ITEM_SLOT || bottomSlot == PROFILE_ITEM_SLOT || bottomSlot == CHALLENGE_ITEM_SLOT) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}

