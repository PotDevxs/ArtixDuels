package dev.artix.artixduels.gui;

import dev.artix.artixduels.managers.CosmeticManager;
import dev.artix.artixduels.models.Cosmetic;
import dev.artix.artixduels.models.PlayerCosmetics;
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
 * GUI para visualizar e selecionar cosméticos.
 */
public class CosmeticGUI implements Listener {
    private final CosmeticManager cosmeticManager;

    public CosmeticGUI(CosmeticManager cosmeticManager) {
        this.cosmeticManager = cosmeticManager;
    }

    /**
     * Abre o menu principal de cosméticos.
     */
    public void openMainMenu(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&d&lCOSMÉTICOS");
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        ItemStack victoryItem = createCategoryItem(Material.FIREWORK, "&e&lEFEITOS DE VITÓRIA",
            "&7Personalize o efeito ao vencer",
            "&7Clique para visualizar");
        gui.setItem(10, victoryItem);

        ItemStack trailItem = createCategoryItem(Material.BLAZE_POWDER, "&b&lTRAILS",
            "&7Rastros de partículas ao se mover",
            "&7Clique para visualizar");
        gui.setItem(12, trailItem);

        ItemStack killItem = createCategoryItem(Material.DIAMOND_SWORD, "&c&lEFEITOS DE KILL",
            "&7Efeitos ao eliminar oponentes",
            "&7Clique para visualizar");
        gui.setItem(14, killItem);

        ItemStack titleItem = createCategoryItem(Material.NAME_TAG, "&6&lTÍTULOS",
            "&7Títulos personalizados",
            "&7Clique para visualizar");
        gui.setItem(16, titleItem);

        ItemStack badgeItem = createCategoryItem(Material.EMERALD, "&a&lBADGES",
            "&7Badges e emblemas",
            "&7Clique para visualizar");
        gui.setItem(28, badgeItem);

        ItemStack closeItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lFECHAR"));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(49, closeItem);

        player.openInventory(gui);
    }

    /**
     * Abre o menu de cosméticos de um tipo específico.
     */
    public void openCosmeticsMenu(Player player, Cosmetic.CosmeticType type) {
        String typeName = getTypeName(type);
        String title = ChatColor.translateAlternateColorCodes('&', "&d&l" + typeName);
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        Inventory gui = Bukkit.createInventory(null, 54, title);

        dev.artix.artixduels.utils.MenuUtils.fillMenuBorders(gui);

        List<Cosmetic> cosmetics = cosmeticManager.getCosmeticsByType(type);
        PlayerCosmetics playerCosmetics = cosmeticManager.getPlayerCosmetics(player.getUniqueId());
        String activeCosmeticId = playerCosmetics.getActiveCosmetic(type);

        int slot = 0;
        for (Cosmetic cosmetic : cosmetics) {
            if (slot >= 45) break;

            boolean unlocked = playerCosmetics.hasCosmeticUnlocked(cosmetic.getId());
            boolean active = cosmetic.getId().equals(activeCosmeticId);
            ItemStack cosmeticItem = createCosmeticItem(cosmetic, unlocked, active, player);
            gui.setItem(slot, cosmeticItem);
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

        if (!title.contains("COSMÉTICOS") && !title.contains("EFEITOS") && 
            !title.contains("TRAILS") && !title.contains("TÍTULOS") && 
            !title.contains("BADGES")) {
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

        if (title.equals(ChatColor.translateAlternateColorCodes('&', "&d&lCOSMÉTICOS"))) {
            if (displayName.contains("VITÓRIA")) {
                openCosmeticsMenu(player, Cosmetic.CosmeticType.VICTORY_EFFECT);
            } else if (displayName.contains("TRAILS")) {
                openCosmeticsMenu(player, Cosmetic.CosmeticType.TRAIL);
            } else if (displayName.contains("KILL")) {
                openCosmeticsMenu(player, Cosmetic.CosmeticType.KILL_EFFECT);
            } else if (displayName.contains("TÍTULOS")) {
                openCosmeticsMenu(player, Cosmetic.CosmeticType.TITLE);
            } else if (displayName.contains("BADGES")) {
                openCosmeticsMenu(player, Cosmetic.CosmeticType.BADGE);
            } else if (displayName.contains("FECHAR")) {
                player.closeInventory();
            }
        } else {
            if (displayName.contains("VOLTAR")) {
                openMainMenu(player);
            } else if (displayName.contains("ATIVAR") || displayName.contains("DESATIVAR")) {
                String cosmeticId = getCosmeticIdFromLore(meta.getLore());
                if (cosmeticId != null) {
                    Cosmetic cosmetic = cosmeticManager.getCosmeticsByType(getTypeFromTitle(title)).stream()
                        .filter(c -> c.getId().equals(cosmeticId))
                        .findFirst()
                        .orElse(null);
                    if (cosmetic != null) {
                        if (displayName.contains("ATIVAR")) {
                            cosmeticManager.setActiveCosmetic(player.getUniqueId(), cosmetic.getType(), cosmetic.getId());
                            player.sendMessage("§aCosmético ativado: §e" + cosmetic.getName());
                            if (cosmetic.getType() == Cosmetic.CosmeticType.TRAIL) {
                                cosmeticManager.startTrail(player);
                            }
                        } else {
                            cosmeticManager.setActiveCosmetic(player.getUniqueId(), cosmetic.getType(), null);
                            player.sendMessage("§cCosmético desativado.");
                            if (cosmetic.getType() == Cosmetic.CosmeticType.TRAIL) {
                                cosmeticManager.stopTrail(player);
                            }
                        }
                        openCosmeticsMenu(player, cosmetic.getType());
                    }
                }
            }
        }
    }

    private String getCosmeticIdFromLore(List<String> lore) {
        if (lore == null) return null;
        for (String line : lore) {
            if (line.contains("ID:")) {
                return ChatColor.stripColor(line).replace("ID: ", "").trim();
            }
        }
        return null;
    }

    private Cosmetic.CosmeticType getTypeFromTitle(String title) {
        if (title.contains("VITÓRIA")) return Cosmetic.CosmeticType.VICTORY_EFFECT;
        if (title.contains("TRAILS")) return Cosmetic.CosmeticType.TRAIL;
        if (title.contains("KILL")) return Cosmetic.CosmeticType.KILL_EFFECT;
        if (title.contains("TÍTULOS")) return Cosmetic.CosmeticType.TITLE;
        if (title.contains("BADGES")) return Cosmetic.CosmeticType.BADGE;
        return Cosmetic.CosmeticType.VICTORY_EFFECT;
    }

    private String getTypeName(Cosmetic.CosmeticType type) {
        switch (type) {
            case VICTORY_EFFECT: return "EFEITOS DE VITÓRIA";
            case TRAIL: return "TRAILS";
            case KILL_EFFECT: return "EFEITOS DE KILL";
            case TITLE: return "TÍTULOS";
            case BADGE: return "BADGES";
            default: return "COSMÉTICOS";
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

    private ItemStack createCosmeticItem(Cosmetic cosmetic, boolean unlocked, boolean active) {
        return createCosmeticItem(cosmetic, unlocked, active, null);
    }

    private ItemStack createCosmeticItem(Cosmetic cosmetic, boolean unlocked, boolean active, Player player) {
        ItemStack item = new ItemStack(cosmetic.getDisplayMaterial(), 1, (short) cosmetic.getDisplayData());
        ItemMeta meta = item.getItemMeta();
        
        String rarityColor = cosmetic.getRarity().getColor();
        String statusPrefix = active ? "&a&l[ATIVO] " : "";
        String unlockPrefix = unlocked ? "" : "&c&l[BLOQUEADO] ";
        
        String cosmeticName = cosmetic.getName();
        String cosmeticDescription = cosmetic.getDescription();
        
        // Processar placeholder <theme> se houver jogador
        if (player != null) {
            try {
                dev.artix.artixduels.managers.ThemeManager themeManager = 
                    ((dev.artix.artixduels.ArtixDuels) org.bukkit.Bukkit.getPluginManager().getPlugin("ArtixDuels")).getThemeManager();
                if (themeManager != null) {
                    cosmeticName = themeManager.processThemePlaceholder(cosmeticName, player.getUniqueId());
                    cosmeticDescription = themeManager.processThemePlaceholder(cosmeticDescription, player.getUniqueId());
                }
            } catch (Exception e) {
                // Ignorar erros
            }
        }
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            statusPrefix + unlockPrefix + rarityColor + cosmeticName));

        List<String> lore = new ArrayList<>();
        lore.add("&8&m                              ");
        lore.add("&7" + cosmeticDescription);
        lore.add("&8&m                              ");
        lore.add("&7Raridade: " + rarityColor + cosmetic.getRarity().getName().toUpperCase());
        lore.add("&8&m                              ");
        
        if (unlocked) {
            if (active) {
                lore.add("&a&l✓ Este cosmético está ativo");
                lore.add("&7Clique para desativar");
            } else {
                lore.add("&a&l✓ Desbloqueado");
                lore.add("&7Clique para ativar");
            }
        } else {
            lore.add("&c&l✗ Bloqueado");
            if (!cosmetic.getUnlockRequirement().isEmpty()) {
                lore.add("&7Requisito: &e" + cosmetic.getUnlockRequirement());
            }
            if (cosmetic.getPrice() > 0) {
                lore.add("&7Preço: &6" + cosmetic.getPrice() + " moedas");
            }
        }
        
        lore.add("&8&m                              ");
        lore.add("&7ID: " + cosmetic.getId());

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        item.setItemMeta(meta);

        return item;
    }
}

