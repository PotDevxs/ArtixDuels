package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.ArenaEditor;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.models.Arena;
import dev.artix.artixduels.models.ArenaTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI para o editor de arenas.
 */
public class ArenaEditorGUI implements Listener {
    private final ArenaEditor arenaEditor;
    private final ArenaManager arenaManager;

    public ArenaEditorGUI(ArenaEditor arenaEditor, ArenaManager arenaManager) {
        this.arenaEditor = arenaEditor;
        this.arenaManager = arenaManager;
    }

    /**
     * Abre o menu principal do editor.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lEDITOR DE ARENAS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        // Criar Nova Arena
        ItemStack createItem = createActionItem(Material.EMERALD, "&a&lCriar Nova Arena",
            "&7Crie uma nova arena do zero");
        gui.setItem(10, createItem);

        // Editar Arena Existente
        ItemStack editItem = createActionItem(Material.BOOK, "&e&lEditar Arena",
            "&7Edite uma arena existente");
        gui.setItem(12, editItem);

        // Templates
        ItemStack templateItem = createActionItem(Material.PAPER, "&b&lTemplates",
            "&7Crie arena a partir de template");
        gui.setItem(14, templateItem);

        // Importar Arena
        ItemStack importItem = createActionItem(Material.HOPPER, "&d&lImportar Arena",
            "&7Importe uma arena de arquivo");
        gui.setItem(16, importItem);

        // Exportar Arena
        ItemStack exportItem = createActionItem(Material.CHEST, "&6&lExportar Arena",
            "&7Exporte uma arena para arquivo");
        gui.setItem(28, exportItem);

        // Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de seleção de arena para editar.
     */
    public void openArenaSelectionMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lSELECIONAR ARENA");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<String, Arena> arenas = arenaManager.getArenas();
        int slot = 0;
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            if (slot >= 45) break;

            ItemStack arenaItem = createArenaItem(entry.getKey(), entry.getValue());
            gui.setItem(slot, arenaItem);
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
        // Título distinto do editor de kits (ambos usavam "TEMPLATES")
        String menuTitle = ChatColor.translateAlternateColorCodes('&', "&6&lTemplates &7Arenas");
        if (menuTitle.length() > 32) {
            menuTitle = menuTitle.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 27, menuTitle);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        Map<String, ArenaTemplate> templates = arenaEditor.getTemplates();
        int slot = 0;
        for (Map.Entry<String, ArenaTemplate> entry : templates.entrySet()) {
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
     * Inventários do editor de arenas (evita confundir com o menu do KitEditor).
     */
    public static boolean isArenaEditorInventoryTitle(String viewTitle) {
        String t = ChatColor.stripColor(viewTitle).toLowerCase();
        if (t.contains("favoritos")) {
            return false;
        }
        if (t.contains("kit") && (t.contains("editor") || t.contains("selecionar") || t.contains("template"))) {
            return false;
        }
        return (t.contains("editor") && t.contains("arena"))
            || (t.contains("selecionar") && t.contains("arena"))
            || (t.contains("template") && t.contains("arena"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String titleRaw = event.getView().getTitle();
        if (!isArenaEditorInventoryTitle(titleRaw)) {
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
        String t = ChatColor.stripColor(titleRaw).toLowerCase();

        if (t.contains("editor") && t.contains("arena")) {
            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else if (displayName.contains("Criar Nova")) {
                player.closeInventory();
                player.sendMessage("§7Use §e/arenaeditor create <nome> §7para criar uma nova arena.");
            } else if (displayName.contains("Editar Arena")) {
                openArenaSelectionMenu(player);
            } else if (displayName.contains("Templates")) {
                openTemplateMenu(player);
            } else if (displayName.contains("Importar")) {
                player.closeInventory();
                player.sendMessage("§7Use §e/arenaeditor import <arquivo> <nome> §7para importar uma arena.");
            } else if (displayName.contains("Exportar")) {
                openArenaSelectionMenu(player);
            }
        } else if (t.contains("selecionar") && t.contains("arena")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String arenaName = getArenaNameFromLore(meta.getLore());
                if (arenaName != null) {
                    if (t.contains("exportar")) {
                        player.closeInventory();
                        player.sendMessage("§7Use §e/arenaeditor export <arena> §7para exportar.");
                    } else {
                        arenaEditor.startEditSession(player, arenaName);
                        player.closeInventory();
                    }
                }
            }
        } else if (t.contains("template") && t.contains("arena")) {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else {
                String templateName = getTemplateNameFromLore(meta.getLore());
                if (templateName != null) {
                    player.closeInventory();
                    player.sendMessage("§7Use §e/arenaeditor template <template> <nome> §7para criar a partir do template.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onMenuInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isArenaEditorInventoryTitle(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        dev.artix.artixduels.models.ArenaEditSession session = arenaEditor.getEditSession(player.getUniqueId());
        
        if (session == null) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        org.bukkit.Location clickedLoc = event.getClickedBlock().getLocation();

        if (displayName.contains("Posição 1")) {
            arenaEditor.setPos1(player, clickedLoc);
            event.setCancelled(true);
        } else if (displayName.contains("Posição 2")) {
            arenaEditor.setPos2(player, clickedLoc);
            event.setCancelled(true);
        } else if (displayName.contains("Spawn Jogador 1")) {
            session.setPlayer1Spawn(clickedLoc);
            player.sendMessage("§aSpawn do jogador 1 definido!");
            event.setCancelled(true);
        } else if (displayName.contains("Spawn Jogador 2")) {
            session.setPlayer2Spawn(clickedLoc);
            player.sendMessage("§aSpawn do jogador 2 definido!");
            event.setCancelled(true);
        } else if (displayName.contains("Spawn Espectador")) {
            session.setSpectatorSpawn(clickedLoc);
            player.sendMessage("§aSpawn do espectador definido!");
            event.setCancelled(true);
        } else if (displayName.contains("Preview")) {
            // Toggle preview já é feito automaticamente
            event.setCancelled(true);
        } else if (displayName.contains("Testar")) {
            arenaEditor.startTestMode(player);
            event.setCancelled(true);
        } else if (displayName.contains("Salvar")) {
            arenaEditor.saveArena(player.getUniqueId());
            event.setCancelled(true);
        } else if (displayName.contains("Cancelar")) {
            arenaEditor.cancelEditSession(player.getUniqueId());
            event.setCancelled(true);
        }
    }

    private String getArenaNameFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Arena:")) {
                return ChatColor.stripColor(line).replace("Arena: ", "").trim();
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

    private ItemStack createArenaItem(String name, Arena arena) {
        ItemStack item = new ItemStack(Material.GRASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + name));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Arena: " + name);
        lore.add("&7Status: " + (arena.isEnabled() ? "&aAtiva" : "&cInativa"));
        lore.add("&7Em uso: " + (arena.isInUse() ? "&cSim" : "&aNão"));
        lore.add("&8&m                              ");
        lore.add("&7Clique para editar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createTemplateItem(ArenaTemplate template) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l" + template.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Template: " + template.getName());
        lore.add("&7Descrição: &e" + template.getDescription());
        lore.add("&7Tamanho: &b" + template.getDefaultSize() + " blocos");
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

