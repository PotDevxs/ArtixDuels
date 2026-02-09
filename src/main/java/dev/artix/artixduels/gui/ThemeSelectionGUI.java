package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.MenuManager;
import dev.artix.artixduels.managers.ThemeManager;
import dev.artix.artixduels.models.Theme;
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
import java.util.Map;

/**
 * GUI para seleção de temas.
 */
public class ThemeSelectionGUI implements Listener {
    private final ThemeManager themeManager;
    private MenuManager menuManager;

    public ThemeSelectionGUI(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    public void setMenuManager(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    /**
     * Abre o menu de seleção de temas.
     */
    public void openThemeMenu(Player player) {
        // Verificar permissão
        if (!player.hasPermission("artixduels.theme.use")) {
            player.sendMessage("§cVocê não tem permissão para usar temas!");
            return;
        }

        // Usar menu do menus.yml se disponível
        if (menuManager != null) {
            MenuManager.MenuData menuData = menuManager.getMenu("theme-selection");
            if (menuData != null) {
                String title = menuData.getTitle();
                if (title.length() > 32) {
                    title = title.substring(0, 32);
                }
                Inventory gui = Bukkit.createInventory(null, menuData.getSize(), title);
                
                // Preencher bordas e itens do menu
                dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);
                
                // Adicionar itens do menu configurável
                for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                    ItemStack item = menuManager.createMenuItem("theme-selection", itemData.getName());
                    if (item != null) {
                        gui.setItem(itemData.getSlot(), item);
                    }
                }
                
                // Adicionar temas disponíveis
                String currentTheme = themeManager.getPlayerTheme(player.getUniqueId());
                Map<String, Theme> themes = themeManager.getThemes();
                
                int slot = 0;
                for (Map.Entry<String, Theme> entry : themes.entrySet()) {
                    if (slot >= 45) break;
                    
                    // Pular slots ocupados por itens do menu
                    boolean slotOccupied = false;
                    for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                        if (itemData.getSlot() == slot) {
                            slotOccupied = true;
                            break;
                        }
                    }
                    if (slotOccupied) {
                        slot++;
                        continue;
                    }
                    
                    Theme theme = entry.getValue();
                    if (theme.isSeasonal()) {
                        List<Theme> seasonal = themeManager.getSeasonalThemes();
                        if (!seasonal.contains(theme)) {
                            continue;
                        }
                    }
                    
                    ItemStack themeItem = createThemeItem(theme, entry.getKey().equals(currentTheme));
                    gui.setItem(slot, themeItem);
                    slot++;
                }
                
                player.openInventory(gui);
                return;
            }
        }

        // Fallback para menu padrão
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lTEMAS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        String currentTheme = themeManager.getPlayerTheme(player.getUniqueId());
        Map<String, Theme> themes = themeManager.getThemes();
        
        int slot = 0;
        for (Map.Entry<String, Theme> entry : themes.entrySet()) {
            if (slot >= 45) break;
            
            Theme theme = entry.getValue();
            if (theme.isSeasonal()) {
                // Verificar se está ativo
                List<Theme> seasonal = themeManager.getSeasonalThemes();
                if (!seasonal.contains(theme)) {
                    continue; // Pular temas sazonais inativos
                }
            }
            
            ItemStack themeItem = createThemeItem(theme, entry.getKey().equals(currentTheme));
            gui.setItem(slot, themeItem);
            slot++;
        }

        // Customizar Tema
        ItemStack customItem = createActionItem(Material.ANVIL, "&d&lCustomizar Tema",
            "&7Crie seu próprio tema",
            "&7personalizado");
        gui.setItem(45, customItem);

        // Preview
        ItemStack previewItem = createActionItem(Material.GLASS, "&b&lPreview",
            "&7Veja uma prévia do tema");
        gui.setItem(47, previewItem);

        // Fechar
        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de customização de tema.
     */
    public void openCustomizeMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&6&lCUSTOMIZAR TEMA");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        String[] colorKeys = {"primary", "secondary", "success", "danger", "warning", "info", "title", "text", "border"};
        String[] colorNames = {"Primária", "Secundária", "Sucesso", "Perigo", "Aviso", "Info", "Título", "Texto", "Borda"};
        
        int slot = 0;
        for (int i = 0; i < colorKeys.length && slot < 45; i++) {
            String currentColor = themeManager.getColor(player.getUniqueId(), colorKeys[i]);
            ItemStack colorItem = createColorItem(colorKeys[i], colorNames[i], currentColor);
            gui.setItem(slot, colorItem);
            slot++;
        }

        // Salvar
        ItemStack saveItem = createActionItem(Material.EMERALD, "&a&lSalvar Tema",
            "&7Salva seu tema customizado");
        gui.setItem(45, saveItem);

        // Voltar
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

        if (!title.contains("TEMAS") && !title.contains("CUSTOMIZAR")) {
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

        if (title.contains("TEMAS")) {
            // Verificar permissão
            if (!player.hasPermission("artixduels.theme.use")) {
                player.sendMessage("§cVocê não tem permissão para usar temas!");
                player.closeInventory();
                return;
            }

            if (displayName.contains("FECHAR")) {
                player.closeInventory();
            } else if (displayName.contains("Customizar")) {
                if (!player.hasPermission("artixduels.theme.customize")) {
                    player.sendMessage("§cVocê não tem permissão para customizar temas!");
                    return;
                }
                openCustomizeMenu(player);
            } else if (displayName.contains("Preview")) {
                player.sendMessage("§7Use §e/theme preview §7para ver uma prévia.");
                player.closeInventory();
            } else {
                String themeName = getThemeNameFromLore(meta.getLore());
                if (themeName != null) {
                    themeManager.setPlayerTheme(player.getUniqueId(), themeName);
                    player.sendMessage("§aTema alterado para: §e" + themeManager.getTheme(themeName).getDisplayName());
                    player.closeInventory();
                }
            }
        } else if (title.contains("CUSTOMIZAR")) {
            if (displayName.contains("VOLTAR")) {
                openThemeMenu(player);
            } else if (displayName.contains("Salvar")) {
                player.sendMessage("§7Use §e/theme customize save <nome> §7para salvar seu tema.");
                player.closeInventory();
            } else {
                String colorKey = getColorKeyFromLore(meta.getLore());
                if (colorKey != null) {
                    player.sendMessage("§7Use §e/theme customize color " + colorKey + " <cor> §7para alterar a cor.");
                    player.closeInventory();
                }
            }
        } else if (title.contains("TEMAS") || title.contains("Selecionar")) {
            // Processar ações do menu configurável
            if (menuManager != null) {
                MenuManager.MenuData menuData = menuManager.getMenu("theme-selection");
                if (menuData != null) {
                    for (MenuManager.MenuItemData itemData : menuData.getItems()) {
                        if (itemData.getSlot() == event.getSlot()) {
                            String action = itemData.getAction();
                            if (action != null && !action.equals("none")) {
                                handleMenuAction(player, action);
                                return;
                            }
                        }
                    }
                }
            }
            
            // Processar seleção de tema
            String themeName = getThemeNameFromLore(meta.getLore());
            if (themeName != null) {
                themeManager.setPlayerTheme(player.getUniqueId(), themeName);
                player.sendMessage("§aTema alterado para: §e" + themeManager.getTheme(themeName).getDisplayName());
                player.closeInventory();
            }
        }
    }

    private void handleMenuAction(Player player, String action) {
        switch (action.toLowerCase()) {
            case "open-customize-theme":
                if (!player.hasPermission("artixduels.theme.customize")) {
                    player.sendMessage("§cVocê não tem permissão para customizar temas!");
                    return;
                }
                openCustomizeMenu(player);
                break;
            case "preview-theme-hint":
                player.sendMessage("§7Use §e/theme preview <tema> §7para ver uma prévia.");
                player.closeInventory();
                break;
            case "close-menu":
                player.closeInventory();
                break;
        }
    }

    private String getThemeNameFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Tema:")) {
                return ChatColor.stripColor(line).replace("Tema: ", "").trim();
            }
        }
        return null;
    }

    private String getColorKeyFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("Chave:")) {
                return ChatColor.stripColor(line).replace("Chave: ", "").trim();
            }
        }
        return null;
    }

    private ItemStack createThemeItem(Theme theme, boolean isSelected) {
        ItemStack item = new ItemStack(Material.BANNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            theme.getColor("title") + theme.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Tema: " + theme.getName());
        lore.add("&7Descrição: &e" + theme.getDescription());
        if (theme.isSeasonal()) {
            lore.add("&7Sazonal: &aSim");
        }
        lore.add("&8&m                              ");
        if (isSelected) {
            lore.add("&a&l✓ SELECIONADO");
        } else {
            lore.add("&7Clique para selecionar");
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

    private ItemStack createColorItem(String colorKey, String colorName, String currentColor) {
        ItemStack item = new ItemStack(Material.WOOL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            currentColor + "&l" + colorName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7Chave: " + colorKey);
        lore.add("&7Cor atual: " + currentColor + "Exemplo");
        lore.add("&8&m                              ");
        lore.add("&7Clique para alterar");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
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
}

