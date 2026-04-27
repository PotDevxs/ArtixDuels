package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.BattlePass;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Comando para ver progresso do passe de batalha e resgatar recompensas.
 */
public class BattlePassCommand implements CommandExecutor {
    private final ArtixDuels plugin;

    public BattlePassCommand(ArtixDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }
        Player player = (Player) sender;
        BattlePass bp = plugin.getBattlePassManager().getCurrentBattlePass();
        if (bp == null || !bp.isActive()) {
            player.sendMessage("§7Não há passe de batalha ativo no momento.");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("claim")) {
            try {
                int level = Integer.parseInt(args[1]);
                plugin.getBattlePassManager().claimReward(player, level);
            } catch (NumberFormatException e) {
                player.sendMessage("§cNível inválido. Use: §e/battlepass claim <nível>");
            }
            return true;
        }

        BattlePass.BattlePassProgress progress = plugin.getBattlePassManager().getPlayerProgress(player.getUniqueId());
        player.sendMessage("§6§l=== Passe de batalha ===");
        player.sendMessage("§7Passe: §f" + bp.getName());
        player.sendMessage("§7Seu nível: §e" + progress.getLevel());
        player.sendMessage("§7XP: §b" + progress.getXp());
        player.sendMessage("§7Premium: " + (progress.isPremium() ? "§aSim" : "§7Não"));
        player.sendMessage("§8");
        player.sendMessage("§7Resgatar recompensa de um nível: §e/battlepass claim <nível>");
        return true;
    }
}
