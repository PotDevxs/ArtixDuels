package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private ArtixDuels plugin;
    private FileConfiguration menuConfig;
    private File menuFile;
    private Map<String, MenuData> menus;

    public MenuManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.menus = new HashMap<>();
        loadMenuConfig();
        loadMenus();
    }

    private void loadMenuConfig() {
        menuFile = new File(plugin.getDataFolder(), "menus.yml");
        if (!menuFile.exists()) {
            plugin.saveResource("menus.yml", false);
        }
        menuConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(menuFile);
    }

    public void reloadMenuConfig() {
        if (menuFile == null) {
            menuFile = new File(plugin.getDataFolder(), "menus.yml");
        }
        menuConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(menuFile);
        menus.clear();
        loadMenus();
    }

    private void loadMenus() {
        ConfigurationSection menusSection = menuConfig.getConfigurationSection("menus");
        if (menusSection == null) return;

        for (String menuName : menusSection.getKeys(false)) {
            ConfigurationSection menuSection = menusSection.getConfigurationSection(menuName);
            if (menuSection == null) continue;

            String title = ChatColor.translateAlternateColorCodes('&', menuSection.getString("title", menuName));
            int size = menuSection.getInt("size", 54);
            
            MenuData menuData = new MenuData(menuName, title, size);
            
            ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String itemName : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemName);
                    if (itemSection == null) continue;

                    int slot = itemSection.getInt("slot", 0);
                    String materialName = itemSection.getString("material", "STONE");
                    Material material = Material.valueOf(materialName.toUpperCase());
                    short data = (short) itemSection.getInt("data", 0);
                    String name = ChatColor.translateAlternateColorCodes('&', itemSection.getString("name", itemName));
                    List<String> lore = new ArrayList<>();
                    if (itemSection.contains("lore")) {
                        for (String loreLine : itemSection.getStringList("lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
                        }
                    }
                    String action = itemSection.getString("action", "");

                    MenuItemData itemData = new MenuItemData(itemName, slot, material, data, name, lore, action);
                    menuData.addItem(itemData);
                }
            }

            menus.put(menuName, menuData);
        }
    }

    public MenuData getMenu(String menuName) {
        return menus.get(menuName);
    }

    public ItemStack createMenuItem(String menuName, String itemName) {
        return createMenuItem(menuName, itemName, null);
    }

    public ItemStack createMenuItem(String menuName, String itemName, org.bukkit.entity.Player player) {
        MenuData menu = menus.get(menuName);
        if (menu == null) return null;

        MenuItemData itemData = menu.getItem(itemName);
        if (itemData == null) return null;

        ItemStack item = new ItemStack(itemData.getMaterial(), 1, itemData.getData());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = itemData.getName();
            List<String> lore = new ArrayList<>(itemData.getLore());
            
            // Processar placeholder <theme> se houver jogador
            if (player != null) {
                displayName = processThemePlaceholder(displayName, player);
                List<String> processedLore = new ArrayList<>();
                for (String loreLine : lore) {
                    processedLore.add(processThemePlaceholder(loreLine, player));
                }
                lore = processedLore;
            }
            
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String processThemePlaceholder(String text, org.bukkit.entity.Player player) {
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = 
                plugin.getThemeManager();
            if (themeManager != null) {
                String themeColor = themeManager.getColor(player.getUniqueId(), "primary");
                return text.replace("<theme>", themeColor);
            }
        } catch (Exception e) {
            // Ignorar erros
        }
        return text.replace("<theme>", "&f");
    }

    public String getMenuTitle(String menuName) {
        MenuData menu = menus.get(menuName);
        if (menu == null) return menuName;
        return menu.getTitle();
    }

    public int getMenuSize(String menuName) {
        MenuData menu = menus.get(menuName);
        if (menu == null) return 54;
        return menu.getSize();
    }

    public List<MenuItemData> getMenuItems(String menuName) {
        MenuData menu = menus.get(menuName);
        if (menu == null) return new ArrayList<>();
        return menu.getItems();
    }

    public static class MenuData {
        private String name;
        private String title;
        private int size;
        private Map<String, MenuItemData> items;

        public MenuData(String name, String title, int size) {
            this.name = name;
            this.title = title;
            this.size = size;
            this.items = new HashMap<>();
        }

        public void addItem(MenuItemData item) {
            items.put(item.getName(), item);
        }

        public MenuItemData getItem(String itemName) {
            return items.get(itemName);
        }

        public List<MenuItemData> getItems() {
            return new ArrayList<>(items.values());
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public int getSize() {
            return size;
        }
    }

    public static class MenuItemData {
        private String name;
        private int slot;
        private Material material;
        private short data;
        private String displayName;
        private List<String> lore;
        private String action;

        public MenuItemData(String name, int slot, Material material, short data, String displayName, List<String> lore, String action) {
            this.name = name;
            this.slot = slot;
            this.material = material;
            this.data = data;
            this.displayName = displayName;
            this.lore = lore;
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public int getSlot() {
            return slot;
        }

        public Material getMaterial() {
            return material;
        }

        public short getData() {
            return data;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getLore() {
            return lore;
        }

        public String getAction() {
            return action;
        }
    }
}

