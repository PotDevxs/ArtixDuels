package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.KitEditor;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.models.Kit;
import dev.artix.artixduels.models.KitTemplate;
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
import java.util.Map;
import java.util.Set;

/**
 * GUI para o editor de kits.
 */
public class KitEditorGUI implements Listener {
    private final KitEditor kitEditor;
    private final KitManager kitManager;

    public KitEditorGUI(KitEditor kitEditor, KitManager kitManager) {
        this.kitEditor = kitEditor;
        this.kitManager = kitManager;
    }

    /**
     * Abre o menu principal do editor.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lEDITOR DE KITS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        // Criar Novo Kit
        ItemStack createItem = createActionItem(Material.EMERALD, "&a&lCriar Novo Kit",
            "&7Crie um novo kit do zero");
        gui.setItem(10, createItem);

        // Editar Kit Existente
        ItemStack editItem = createActionItem(Material.BOOK, "&e&lEditar Kit",
            "&7Edite um kit existente");
        gui.setItem(12, editItem);

        // Templates
        ItemStack templateItem = createActionItem(Material.PAPER, "&b&lTemplates",
            "&7Crie kit a partir de template");
        gui.setItem(14, templateItem);

        // Favoritos
        ItemStack favoritesItem = createActionItem(Material.NETHER_STAR, "&d&lFavoritos",
            "&7Veja seus kits favoritos");
        gui.setItem(16, favoritesItem);

        // Importar Kit
        ItemStack importItem = createActionItem(Material.HOPPER, "&d&lImportar Kit",
            "&7Importe um kit de arquivo");
        gui.setItem(28, importItem);

        // Exportar Kit
        ItemStack exportItem = createActionItem(Material.CHEST, "&6&lExportar Kit",
            "&7Exporte um kit para arquivo");
        gui.setItem(30, exportItem);

        // Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de seleção de kits.
     */
    public void openKitSelectionMenu(Player player, boolean forEdit) {
        String title = ChatColor.translateAlternateColorCodes('&', forEdit ? "&6&lEDITAR KIT" : "&6&lSELECIONAR KIT");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<String, Kit> kits = kitManager.getKits();
        int slot = 0;
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            if (slot >= 45) break;

            ItemStack kitItem = createKitItem(entry.getKey(), entry.getValue(), player.getUniqueId());
            gui.setItem(slot, kitItem);
            slot++;
        }

        // Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de templates.
     */
    public void openTemplateMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lTemplates &7Kits");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 27, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<String, KitTemplate> templates = kitEditor.getTemplates();
        int slot = 0;
        for (Map.Entry<String, KitTemplate> entry : templates.entrySet()) {
            if (slot >= 18) break;

            ItemStack templateItem = createTemplateItem(entry.getValue());
            gui.setItem(slot, templateItem);
            slot++;
        }

        // Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(18, backItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de favoritos.
     */
    public void openFavoritesMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lFAVORITOS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Set<String> favorites = kitEditor.getFavorites(player.getUniqueId());
        int slot = 0;
        for (String kitName : favorites) {
            if (slot >= 45) break;

            Kit kit = kitManager.getKit(kitName);
            if (kit != null) {
                ItemStack kitItem = createKitItem(kitName, kit, player.getUniqueId());
                gui.setItem(slot, kitItem);
                slot++;
            }
        }

        // Voltar
        ItemStack backItem = new ItemStack(Material.PAPER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lVOLTAR"));
        backItem.setItemMeta(backMeta);
        gui.setItem(49, backItem);

        player.openInventory(gui);
    }

    private static boolean isKitEditorInventoryTitle(String viewTitle) {
        if (ArenaEditorGUI.isArenaEditorInventoryTitle(viewTitle)) {
            return false;
        }
        String t = ChatColor.stripColor(viewTitle).toLowerCase();
        if (t.contains("favoritos")) {
            return true;
        }
        return t.contains("kit")
            && (t.contains("editor") || t.contains("selecionar") || t.contains("template") || t.contains("editar") || t.contains("exportar"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!isKitEditorInventoryTitle(title)) {
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
        String t = ChatColor.stripColor(title).toLowerCase();

        if (t.contains("editor") && t.contains("kit")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else if (displayName.contains("Criar Novo")) {
                player.closeInventory();
                player.sendMessage("§7Use §e/kiteditor create <nome> §7para criar um novo kit.");
            } else if (displayName.contains("Editar Kit")) {
                openKitSelectionMenu(player, true);
            } else if (displayName.contains("Templates")) {
                openTemplateMenu(player);
            } else if (displayName.contains("Favoritos")) {
                openFavoritesMenu(player);
            } else if (displayName.contains("Importar")) {
                player.closeInventory();
                player.sendMessage("§7Use §e/kiteditor import <arquivo> <nome> §7para importar um kit.");
            } else if (displayName.contains("Exportar")) {
                openKitSelectionMenu(player, false);
            }
        } else if (t.contains("editar") && t.contains("kit") || t.contains("selecionar") && t.contains("kit")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String kitName = getKitNameFromLore(meta.getLore());
                if (kitName != null) {
                    if (t.contains("exportar") && t.contains("selecionar")) {
                        player.closeInventory();
                        player.sendMessage("§7Use §e/kiteditor export <kit> §7para exportar.");
                    } else if (t.contains("editar")) {
                        kitEditor.startEditSession(player, kitName);
                        player.closeInventory();
                    } else {
                        kitEditor.startPreview(player, kitName);
                        player.closeInventory();
                    }
                }
            }
        } else if (t.contains("template") && t.contains("kit")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String templateName = getTemplateNameFromLore(meta.getLore());
                if (templateName != null) {
                    player.closeInventory();
                    player.sendMessage("§7Use §e/kiteditor template <template> <nome> §7para criar a partir do template.");
                }
            }
        } else if (t.contains("favoritos")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String kitName = getKitNameFromLore(meta.getLore());
                if (kitName != null) {
                    if (kitEditor.isFavorite(player.getUniqueId(), kitName)) {
                        kitEditor.removeFavorite(player.getUniqueId(), kitName);
                        player.sendMessage("§cKit removido dos favoritos!");
                    } else {
                        kitEditor.addFavorite(player.getUniqueId(), kitName);
                        player.sendMessage("§aKit adicionado aos favoritos!");
                    }
                    openFavoritesMenu(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onMenuInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isKitEditorInventoryTitle(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
    }

    private String getKitNameFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Kit:")) {
                return ChatColor.stripColor(line).replace("Kit: ", "").trim();
            }
        }
        return null;
    }

    private String getTemplateNameFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Template:")) {
                return ChatColor.stripColor(line).replace("Template: ", "").trim();
            }
        }
        return null;
    }

    private ItemStack createActionItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> loreList = new ArrayList<>();
        loreList.add("&8&m                              ");
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        loreList.add("&8&m                              ");
        loreList.add("&7Clique para usar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : loreList) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createKitItem(String name, Kit kit, java.util.UUID playerId) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + kit.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Kit: " + name);
        lore.add("&7Modo: &e" + kit.getMode().getDisplayName());
        lore.add("&8&m                              ");
        
        boolean isFavorite = kitEditor.isFavorite(playerId, name);
        if (isFavorite) {
            lore.add("&d&l★ FAVORITO");
        }
        
        lore.add("&7Clique para usar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createTemplateItem(KitTemplate template) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + template.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Template: " + template.getName());
        lore.add("&7Descrição: &e" + template.getDescription());
        lore.add("&7Modo: &b" + template.getMode().getDisplayName());
        lore.add("&8&m                              ");
        lore.add("&7Clique para usar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }
}

