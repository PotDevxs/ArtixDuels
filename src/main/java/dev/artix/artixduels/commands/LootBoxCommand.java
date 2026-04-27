package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Lista e abre loot boxes do jogador.
 */
public class LootBoxCommand implements CommandExecutor {
    private final ArtixDuels plugin;

    public LootBoxCommand(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }
        Player player = (Player) sender;
        List<String> owned = plugin.getLootBoxManager().getPlayerLootBoxes(player.getUniqueId());

        if (args.length >= 2 && args[0].equalsIgnoreCase("open")) {
            String id = args[1];
            if (!owned.contains(id)) {
                player.sendMessage("§cVocê não possui esta loot box. Use §e/lootbox §cpara listar.");
                return true;
            }
            plugin.getLootBoxManager().openLootBox(player, id);
            return true;
        }

        if (owned.isEmpty()) {
            player.sendMessage("§7Você não possui loot boxes.");
            if (!plugin.getLootBoxManager().getRegisteredLootBoxIds().isEmpty()) {
                player.sendMessage("§8IDs no servidor: §f" + String.join(", ", plugin.getLootBoxManager().getRegisteredLootBoxIds()));
            }
            return true;
        }
        player.sendMessage("§6§l=== Suas loot boxes ===");
        for (String id : owned) {
            player.sendMessage("§7- §e" + id + " §8| §7/lootbox open " + id);
        }
        return true;
    }
}
