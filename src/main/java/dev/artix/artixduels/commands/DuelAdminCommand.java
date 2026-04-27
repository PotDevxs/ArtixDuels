package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.gui.ConfigGUI;
import dev.artix.artixduels.managers.ArenaManager;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.KitManager;
import dev.artix.artixduels.managers.MessageManager;
import dev.artix.artixduels.managers.StatsManager;
import dev.artix.artixduels.models.Arena;
import dev.artix.artixduels.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelAdminCommand implements CommandExecutor {
    private ArtixDuels plugin;
    private DuelManager duelManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private MessageManager messageManager;
    private ConfigGUI configGUI;

    public DuelAdminCommand(ArtixDuels plugin, DuelManager duelManager, KitManager kitManager, ArenaManager arenaManager, StatsManager statsManager, MessageManager messageManager, ConfigGUI configGUI) {
        this.plugin = plugin;
        this.duelManager = duelManager;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.messageManager = messageManager;
        this.configGUI = configGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("artixduels.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                configGUI.openMainMenu((Player) sender);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "menu":
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getMessage("error.only-players"));
                    return true;
                }
                configGUI.openMainMenu((Player) sender);
                break;

            case "reload":
                plugin.reloadConfig();
                plugin.reloadScoreboardConfig();
                plugin.reloadTablistConfig();
                plugin.reloadNPCsConfig();
                plugin.reloadMessagesConfig();
                plugin.reloadKitsConfig();
                plugin.reloadMenuConfig();
                sender.sendMessage(messageManager.getMessage("admin.config-reloaded"));
                break;

            case "setarena":
                if (args.length < 2) {
                    sender.sendMessage(messageManager.getMessage("error.usage", 
                        createMap("usage", "/dueladmin setarena <nome>")));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getMessage("error.only-players"));
                    return true;
                }
                handleSetArena((Player) sender, args[1]);
                break;

            case "setkit":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getMessage("error.only-players"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(messageManager.getMessage("error.usage", 
                        createMap("usage", "/dueladmin setkit <nome>")));
                    return true;
                }
                handleSetKit((Player) sender, args[1]);
                break;

            case "setspawn":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(messageManager.getMessage("error.only-players"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(messageManager.getMessage("error.usage", 
                        createMap("usage", "/dueladmin setspawn <arena> <1|2|spectator>")));
                    return true;
                }
                handleSetSpawn((Player) sender, args[1], args[2]);
                break;

            case "forcestop":
                if (args.length < 2) {
                    sender.sendMessage("§cUso: /dueladmin forcestop <jogador>");
                    return true;
                }
                handleForceStop(args[1]);
                break;

            case "resetstats":
                if (args.length < 2) {
                    sender.sendMessage("§cUso: /dueladmin resetstats <jogador>");
                    return true;
                }
                handleResetStats(args[1]);
                break;

            case "givebox":
            case "darcaixa":
                if (args.length < 3) {
                    sender.sendMessage("§cUso: /dueladmin givebox <jogador> <id>");
                    sender.sendMessage("§7IDs: §f" + String.join(", ", plugin.getLootBoxManager().getRegisteredLootBoxIds()));
                    return true;
                }
                handleGiveLootBox(sender, args[1], args[2]);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§7§m----§f §bComandos Admin §7§m----§f");
        sender.sendMessage("§e/dueladmin menu §7- Abre o menu de configuração");
        sender.sendMessage("§e/dueladmin reload §7- Recarrega a configuração");
        sender.sendMessage("§e/dueladmin setarena <nome> §7- Define uma arena na sua localização");
        sender.sendMessage("§e/dueladmin setkit <nome> §7- Salva seu inventário como kit");
        sender.sendMessage("§e/dueladmin setspawn <arena> <1|2|spectator> §7- Define spawn de uma arena");
        sender.sendMessage("§e/dueladmin forcestop <jogador> §7- Força parar um duelo");
        sender.sendMessage("§e/dueladmin resetstats <jogador> §7- Reseta estatísticas de um jogador");
        sender.sendMessage("§e/dueladmin givebox <jogador> <id> §7- Dá uma loot box (ex.: common, rare)");
    }

    private java.util.Map<String, String> createMap(String... pairs) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (i + 1 < pairs.length) {
                map.put(pairs[i], pairs[i + 1]);
            }
        }
        return map;
    }

    private void handleSetArena(Player player, String arenaName) {
        Location loc = player.getLocation();
        Arena arena = new Arena(arenaName);
        arena.setPlayer1Spawn(loc.clone().add(5, 0, 0));
        arena.setPlayer2Spawn(loc.clone().add(-5, 0, 0));
        arena.setSpectatorSpawn(loc.clone().add(0, 5, 0));

        arenaManager.addArena(arenaName, arena);
        arenaManager.saveArena(arenaName, arena);
        player.sendMessage(messageManager.getMessage("admin.arena-created", 
            createMap("arena", arenaName)));
    }

    private void handleSetKit(Player player, String kitName) {
        Kit kit = new Kit(kitName, kitName);
        kit.setContents(player.getInventory().getContents().clone());
        kit.setArmor(player.getInventory().getArmorContents().clone());

        kitManager.addKit(kitName, kit);
        kitManager.saveKit(kitName, kit);
        player.sendMessage(messageManager.getMessage("admin.kit-saved", 
            createMap("kit", kitName)));
    }

    private void handleSetSpawn(Player player, String arenaName, String spawnType) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            player.sendMessage(messageManager.getMessage("error.arena-not-found"));
            return;
        }

        Location loc = player.getLocation();
        switch (spawnType.toLowerCase()) {
            case "1":
                arena.setPlayer1Spawn(loc);
                player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                    createMap("type", "Jogador 1", "arena", arenaName)));
                break;
            case "2":
                arena.setPlayer2Spawn(loc);
                player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                    createMap("type", "Jogador 2", "arena", arenaName)));
                break;
            case "spectator":
            case "spec":
                arena.setSpectatorSpawn(loc);
                player.sendMessage(messageManager.getMessage("admin.spawn-set", 
                    createMap("type", "Espectador", "arena", arenaName)));
                break;
            default:
                player.sendMessage(messageManager.getMessage("error.invalid-spawn-type"));
                break;
        }
        arenaManager.saveArena(arenaName, arena);
    }

    private void handleForceStop(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage("§cJogador não encontrado!");
            return;
        }

        if (duelManager.isInDuel(player)) {
            duelManager.endDuel(player.getUniqueId(), player.getUniqueId(), true);
            Bukkit.getConsoleSender().sendMessage("§aDuelo forçado a parar para " + playerName);
        } else {
            Bukkit.getConsoleSender().sendMessage("§cO jogador não está em um duelo!");
        }
    }

    private void handleResetStats(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage("§cJogador não encontrado!");
            return;
        }

        statsManager.removeCachedStats(player.getUniqueId());
        Bukkit.getConsoleSender().sendMessage("§aEstatísticas de §e" + playerName + " §aresetadas!");
    }

    @SuppressWarnings("deprecation")
    private void handleGiveLootBox(CommandSender sender, String playerName, String boxId) {
        if (!plugin.getLootBoxManager().getRegisteredLootBoxIds().contains(boxId)) {
            sender.sendMessage("§cLoot box desconhecida: §e" + boxId);
            sender.sendMessage("§7Disponíveis: §f" + String.join(", ", plugin.getLootBoxManager().getRegisteredLootBoxIds()));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cJogador não encontrado ou nunca entrou no servidor.");
            return;
        }

        plugin.getLootBoxManager().giveLootBox(target.getUniqueId(), boxId);

        Player online = target.getPlayer();
        if (online != null && online.isOnline()) {
            online.sendMessage("§6§l[Loot Box] §aVocê recebeu: §e" + boxId);
        }
        sender.sendMessage("§aLoot box §e" + boxId + " §adada para §f" + playerName + "§a.");
    }
}

