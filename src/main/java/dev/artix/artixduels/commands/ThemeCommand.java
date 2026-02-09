package dev.artix.artixduels.commands;

import dev.artix.artixduels.gui.ThemeSelectionGUI;
import dev.artix.artixduels.managers.ThemeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Comando para gerenciar temas.
 */
public class ThemeCommand implements CommandExecutor {
    private final ThemeManager themeManager;
    private final ThemeSelectionGUI themeGUI;
    private Map<Player, Map<String, String>> customizingThemes;

    public ThemeCommand(ThemeManager themeManager, ThemeSelectionGUI themeGUI) {
        this.themeManager = themeManager;
        this.themeGUI = themeGUI;
        this.customizingThemes = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        // Verificar permissão
        if (!player.hasPermission("artixduels.theme.use")) {
            player.sendMessage("§cVocê não tem permissão para usar temas!");
            return true;
        }

        if (args.length == 0) {
            themeGUI.openThemeMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /theme set <tema>");
                    return true;
                }
                String themeName = args[1];
                if (themeManager.getTheme(themeName) == null) {
                    player.sendMessage("§cTema não encontrado!");
                    return true;
                }
                themeManager.setPlayerTheme(player.getUniqueId(), themeName);
                player.sendMessage("§aTema alterado para: §e" + themeManager.getTheme(themeName).getDisplayName());
                break;

            case "preview":
                if (args.length < 2) {
                    player.sendMessage("§cUso: /theme preview <tema>");
                    return true;
                }
                String previewTheme = args[1];
                dev.artix.artixduels.models.Theme theme = themeManager.getTheme(previewTheme);
                if (theme == null) {
                    player.sendMessage("§cTema não encontrado!");
                    return true;
                }
                showPreview(player, theme);
                break;

            case "customize":
                if (!player.hasPermission("artixduels.theme.customize")) {
                    player.sendMessage("§cVocê não tem permissão para customizar temas!");
                    return true;
                }
                if (args.length < 2) {
                    themeGUI.openCustomizeMenu(player);
                    return true;
                }
                if (args[1].equalsIgnoreCase("save")) {
                    if (args.length < 3) {
                        player.sendMessage("§cUso: /theme customize save <nome>");
                        return true;
                    }
                    Map<String, String> colors = customizingThemes.get(player);
                    if (colors == null) {
                        player.sendMessage("§cVocê não está customizando um tema!");
                        return true;
                    }
                    themeManager.createCustomTheme(player.getUniqueId(), args[2], colors);
                    customizingThemes.remove(player);
                    player.sendMessage("§aTema customizado salvo!");
                } else if (args[1].equalsIgnoreCase("color")) {
                    if (args.length < 4) {
                        player.sendMessage("§cUso: /theme customize color <chave> <cor>");
                        return true;
                    }
                    String colorKey = args[2];
                    String color = args[3];
                    if (!color.startsWith("&")) {
                        color = "&" + color;
                    }
                    customizingThemes.computeIfAbsent(player, k -> new HashMap<>()).put(colorKey, color);
                    player.sendMessage("§aCor §e" + colorKey + " §adefinida para: " + ChatColor.translateAlternateColorCodes('&', color + "Exemplo"));
                } else {
                    themeGUI.openCustomizeMenu(player);
                }
                break;

            case "list":
                player.sendMessage("§6=== Temas Disponíveis ===");
                for (Map.Entry<String, dev.artix.artixduels.models.Theme> entry : themeManager.getThemes().entrySet()) {
                    dev.artix.artixduels.models.Theme t = entry.getValue();
                    if (t.isSeasonal()) {
                        if (themeManager.getSeasonalThemes().contains(t)) {
                            player.sendMessage("§e" + entry.getKey() + " §7- §a" + t.getDisplayName() + " §7(Sazonal)");
                        }
                    } else {
                        player.sendMessage("§e" + entry.getKey() + " §7- §a" + t.getDisplayName());
                    }
                }
                break;

            default:
                themeGUI.openThemeMenu(player);
                break;
        }

        return true;
    }

    private void showPreview(Player player, dev.artix.artixduels.models.Theme theme) {
        player.sendMessage("§6=== Preview do Tema: " + theme.getDisplayName() + " ===");
        player.sendMessage(themeManager.applyTheme("{primary}Cor Primária", theme));
        player.sendMessage(themeManager.applyTheme("{secondary}Cor Secundária", theme));
        player.sendMessage(themeManager.applyTheme("{success}Cor de Sucesso", theme));
        player.sendMessage(themeManager.applyTheme("{danger}Cor de Perigo", theme));
        player.sendMessage(themeManager.applyTheme("{warning}Cor de Aviso", theme));
        player.sendMessage(themeManager.applyTheme("{info}Cor de Info", theme));
        player.sendMessage(themeManager.applyTheme("{title}Título", theme));
        player.sendMessage(themeManager.applyTheme("{text}Texto", theme));
    }
}

