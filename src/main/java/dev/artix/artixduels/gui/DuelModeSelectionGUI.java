package dev.artix.artixduels.gui;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.MenuManager;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelModeSelectionGUI implements Listener {
    private DuelManager duelManager;
    private KitManager kitManager;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private Map<UUID, String> pendingChallenges;

    public DuelModeSelectionGUI(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, MessageManager messageManager, MenuManager menuManager) {
        this.duelManager = duelManager;
        this.kitManager = kitManager;
        this.messageManager = messageManager;
        this.menuManager = menuManager;
        this.pendingChallenges = new HashMap<>();
    }

    public void openModeSelectionMenu(Player challenger, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            challenger.sendMessage(messageManager.getMessage("error.player-not-found"));
            return;
        }

        if (target.equals(challenger)) {
            challenger.sendMessage(messageManager.getMessage("error.cannot-challenge-self"));
            return;
        }

        pendingChallenges.put(challenger.getUniqueId(), targetName);

        MenuManager.MenuData menuData = menuManager.getMenu("duel-mode-selection");
        String title = menuData != null ? menuData.getTitle() : "&6&lModo de Duelo";
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        int size = menuData != null ? menuData.getSize() : 54;
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Preencher bordas do menu
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);
        
        int slot = 0;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;
            
            ItemStack modeItem = createModeItem(mode);
            gui.setItem(slot, modeItem);
            slot++;
        }
        
        // Adicionar itens do menu configurável
        if (menuData != null) {
            for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                ItemStack item = menuManager.createMenuItem("duel-mode-selection", itemData.getName());
                if (item != null) {
                    gui.setItem(itemData.getSlot(), item);
                }
            }
        } else {
            // Fallback
            ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta closeMeta = closeItem.getItemMeta();
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messageManager.getMessageNoPrefix("gui.close")));
            closeMeta.setLore(java.util.Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7"), 
                ChatColor.translateAlternateColorCodes('&', "&7Clique para fechar o menu"), 
                ChatColor.translateAlternateColorCodes('&', "&7")));
            closeItem.setItemMeta(closeMeta);
            gui.setItem(49, closeItem);
        }
        
        challenger.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Modo de Duelo")) {
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
            pendingChallenges.remove(player.getUniqueId());
            return;
        }
        
        String targetName = pendingChallenges.get(player.getUniqueId());
        if (targetName == null) {
            player.closeInventory();
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage(messageManager.getMessage("error.player-not-found"));
            player.closeInventory();
            pendingChallenges.remove(player.getUniqueId());
            return;
        }
        
        DuelMode selectedMode = getModeFromDisplayName(displayName);
        if (selectedMode == null) {
            return;
        }
        
        player.closeInventory();
        pendingChallenges.remove(player.getUniqueId());
        
        String defaultKit = getDefaultKit();
        if (defaultKit == null) {
            player.sendMessage(messageManager.getMessage("error.kit-not-found"));
            return;
        }
        
        duelManager.sendDuelRequest(player, target, defaultKit, null, selectedMode);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String title = event.getView().getTitle();
            
            if (title.contains("Modo de Duelo")) {
                pendingChallenges.remove(player.getUniqueId());
            }
        }
    }

    private ItemStack createModeItem(DuelMode mode) {
        Material material = getMaterialForMode(mode);
        String displayName = "&6&l" + mode.getDisplayName();
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Clique para desafiar com");
        lore.add("&7o modo &e" + mode.getDisplayName());
        
        return createMenuItem(material, displayName, lore.toArray(new String[0]));
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

    private DuelMode getModeFromDisplayName(String displayName) {
        for (DuelMode mode : DuelMode.values()) {
            if (displayName.contains(mode.getDisplayName())) {
                return mode;
            }
        }
        return null;
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

    private String getDefaultKit() {
        if (kitManager.getKits().isEmpty()) {
            return null;
        }
        return kitManager.getKits().keySet().iterator().next();
    }

    public void openQueueMenu(Player player) {
        if (menuManager == null) {
            // Fallback se menuManager não estiver inicializado
            String title = ChatColor.translateAlternateColorCodes('&', "&6&lProcurar Partida");
            if (title.length() > 32) {
                title = title.substring(0, 32);
            }
            Inventory gui = Bukkit.createInventory(null, 54, title);
            
            // Preencher bordas do menu
            dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);
            
            int slot = 0;
            for (DuelMode mode : DuelMode.values()) {
                if (slot >= 45) break;
                
                ItemStack modeItem = createQueueModeItem(mode);
                gui.setItem(slot, modeItem);
                slot++;
            }
            
            ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta closeMeta = closeItem.getItemMeta();
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messageManager != null ? messageManager.getMessageNoPrefix("gui.close") : "&c&lFechar"));
            closeMeta.setLore(java.util.Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7"), 
                ChatColor.translateAlternateColorCodes('&', "&7Clique para fechar o menu"), 
                ChatColor.translateAlternateColorCodes('&', "&7")));
            closeItem.setItemMeta(closeMeta);
            gui.setItem(49, closeItem);
            
            player.openInventory(gui);
            return;
        }
        
        MenuManager.MenuData menuData = menuManager.getMenu("queue-mode-selection");
        String title = menuData != null ? menuData.getTitle() : "&6&lProcurar Partida";
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        int size = menuData != null ? menuData.getSize() : 54;
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Preencher bordas do menu
        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);
        
        int slot = 0;
        for (DuelMode mode : DuelMode.values()) {
            if (slot >= 45) break;
            
            ItemStack modeItem = createQueueModeItem(mode);
            gui.setItem(slot, modeItem);
            slot++;
        }
        
        // Adicionar itens do menu configurável
        if (menuData != null) {
            for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                ItemStack item = menuManager.createMenuItem("queue-mode-selection", itemData.getName());
                if (item != null) {
                    gui.setItem(itemData.getSlot(), item);
                }
            }
        } else {
            // Fallback
            ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta closeMeta = closeItem.getItemMeta();
            closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messageManager.getMessageNoPrefix("gui.close")));
            closeMeta.setLore(java.util.Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&7"), 
                ChatColor.translateAlternateColorCodes('&', "&7Clique para fechar o menu"), 
                ChatColor.translateAlternateColorCodes('&', "&7")));
            closeItem.setItemMeta(closeMeta);
            gui.setItem(49, closeItem);
        }
        
        player.openInventory(gui);
    }

    private ItemStack createQueueModeItem(DuelMode mode) {
        Material material = getMaterialForMode(mode);
        String displayName = "&6&l" + mode.getDisplayName();
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Clique para entrar na fila");
        lore.add("&7do modo &e" + mode.getDisplayName());
        
        return createMenuItem(material, displayName, lore.toArray(new String[0]));
    }

    @EventHandler
    public void onQueueInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (!title.contains("Procurar Partida") && !title.contains("Queue")) {
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
            return;
        }
        
        DuelMode selectedMode = getModeFromDisplayName(displayName);
        if (selectedMode == null) {
            return;
        }
        
        player.closeInventory();
        duelManager.addToMatchmaking(player, selectedMode);
    }

    @EventHandler
    public void onMenuInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (title.contains("Procurar Partida") || title.contains("Queue")
                || title.contains("Modo de Duelo")) {
            event.setCancelled(true);
        }
    }
}

