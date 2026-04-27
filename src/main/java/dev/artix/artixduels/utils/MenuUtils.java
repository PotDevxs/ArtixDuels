package dev.artix.artixduels.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuUtils {

    public static void fillMenuBorders(Inventory gui) {
        ItemStack borderTop = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
        ItemMeta borderTopMeta = borderTop.getItemMeta();
        borderTopMeta.setDisplayName(" ");
        borderTop.setItemMeta(borderTopMeta);

        ItemStack borderSide = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta borderSideMeta = borderSide.getItemMeta();
        borderSideMeta.setDisplayName(" ");
        borderSide.setItemMeta(borderSideMeta);
        
        int size = gui.getSize();
        for (int i = 0; i < 9; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, borderTop.clone());
            }
        }
        for (int i = size - 9; i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, borderTop.clone());
            }
        }
        for (int i = 9; i < size - 9; i += 9) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, borderSide.clone());
            }
            if (gui.getItem(i + 8) == null) {
                gui.setItem(i + 8, borderSide.clone());
            }
        }
    }
}

