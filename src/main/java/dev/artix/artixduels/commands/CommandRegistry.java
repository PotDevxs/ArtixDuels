package dev.artix.artixduels.commands;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe responsável por registrar todos os comandos do plugin programaticamente.
 * Organiza os comandos em categorias: Player e Admin.
 */
public class CommandRegistry {
    private final ArtixDuels plugin;
    private final CommandMap commandMap;

    public CommandRegistry(ArtixDuels plugin) {
        this.plugin = plugin;
        this.commandMap = getCommandMap();
    }

    /**
     * Obtém o CommandMap do servidor usando reflection.
     * @return CommandMap ou null se houver erro
     */
    private CommandMap getCommandMap() {
        try {
            SimplePluginManager pluginManager = (SimplePluginManager) plugin.getServer().getPluginManager();
            Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(pluginManager);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao obter CommandMap: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registra um comando no CommandMap.
     * @param name Nome do comando
     * @param description Descrição do comando
     * @param aliases Lista de aliases
     * @param permission Permissão necessária (null se não houver)
     * @param executor Executor do comando
     * @param tabCompleter TabCompleter do comando (null se não houver)
     */
    public void registerCommand(String name, String description, List<String> aliases, 
                                 String permission, CommandExecutor executor, TabCompleter tabCompleter) {
        if (commandMap == null) {
            plugin.getLogger().warning("CommandMap não disponível, não foi possível registrar o comando: " + name);
            return;
        }

        Command command = new Command(name, description, "/" + name, new ArrayList<>(aliases)) {
            @Override
            public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    sender.sendMessage("§cVocê não tem permissão para usar este comando!");
                    return true;
                }
                return executor.onCommand(sender, this, commandLabel, args);
            }

            @Override
            public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                if (tabCompleter != null) {
                    return tabCompleter.onTabComplete(sender, this, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };

        if (permission != null && !permission.isEmpty()) {
            command.setPermission(permission);
        }

        commandMap.register("artixduels", command);
    }

    /**
     * Registra todos os comandos do plugin.
     * Organizados por categoria: Player e Admin.
     */
    public void registerAllCommands() {
        registerPlayerCommands();
        registerAdminCommands();
        plugin.getLogger().info("Todos os comandos foram registrados com sucesso!");
    }

    /**
     * Registra comandos disponíveis para todos os jogadores.
     */
    private void registerPlayerCommands() {
        // Comando principal de duelos
        registerCommand("duelo", "Comando principal de duelos",
            Arrays.asList("duel", "duelos"), null,
            new DuelCommand(plugin, plugin.getDuelManager(), plugin.getKitManager(), 
                          plugin.getArenaManager(), plugin.getDuelModeSelectionGUI()),
            new DuelTabCompleter());

        // Aceitar convite
        registerCommand("accept", "Aceitar um convite de duelo",
            Arrays.asList("aceitar"), null,
            new AcceptCommand(plugin.getDuelManager()), null);

        // Recusar convite
        registerCommand("deny", "Recusar um convite de duelo",
            Arrays.asList("recusar"), null,
            new DenyCommand(plugin.getDuelManager()), null);

        // Estatísticas
        registerCommand("stats", "Ver estatísticas de duelos",
            Arrays.asList("estatisticas", "estatisticas"), null,
            new StatsCommand(plugin.getStatsManager()), null);

        // Espectar duelo
        registerCommand("spectate", "Espectar um duelo",
            Arrays.asList("espectar", "spec"), null,
            new SpectateCommand(plugin.getDuelManager(), plugin.getSpectatorManager()), null);

        // Histórico
        registerCommand("history", "Ver histórico de duelos",
            Arrays.asList("historico"), null,
            new HistoryCommand(plugin.getHistoryDAO()), null);

        // Scoreboard
        registerCommand("scoreboard", "Configurar modos do scoreboard",
            Arrays.asList("sb", "score"), null,
            new ScoreboardCommand(plugin.getScoreboardModeSelectionGUI()), null);

        // Teleportar para lobby
        registerCommand("spawn", "Teleportar para o lobby",
            Arrays.asList("lobby"), null,
            new SpawnCommand(plugin), null);

            // Entrar na fila
            registerCommand("queue", "Entrar na fila de matchmaking",
                Arrays.asList("fila", "matchmaking"), null,
                new QueueCommand(plugin, plugin.getDuelManager(), plugin.getDuelModeSelectionGUI()),
                new QueueTabCompleter());

            // Rankings
            registerCommand("ranking", "Ver rankings e leaderboards",
                Arrays.asList("rankings", "top", "leaderboard"), null,
                new RankingCommand(plugin.getRankingGUI()), null);

            // Desafios
            registerCommand("challenges", "Ver desafios diários e semanais",
                Arrays.asList("desafios", "challenge"), null,
                new ChallengeCommand(plugin.getChallengeGUI()), null);

            // Cosméticos
            registerCommand("cosmetics", "Ver e gerenciar cosméticos",
                Arrays.asList("cosmeticos", "cosmetic"), null,
                new CosmeticCommand(plugin.getCosmeticGUI()), null);

            // Torneios
            registerCommand("tournament", "Ver e gerenciar torneios",
                Arrays.asList("torneio", "torneios", "tourney"), null,
                new TournamentCommand(plugin, plugin.getTournamentManager(), plugin.getTournamentGUI()), null);

            // Replays
            registerCommand("replay", "Ver e gerenciar replays",
                Arrays.asList("replays", "replay"), null,
                new ReplayCommand(plugin, plugin.getReplayManager(), plugin.getReplayGUI()), null);

            // Treinamento
            registerCommand("training", "Treinar contra bots",
                Arrays.asList("treinar", "train", "bot"), null,
                new TrainingCommand(plugin, plugin.getTrainingManager(), plugin.getTrainingGUI()), null);

            // Dashboard de Estatísticas
            registerCommand("dashboard", "Ver dashboard de estatísticas",
                Arrays.asList("stats", "estatisticas", "estat"), null,
                new DashboardCommand(plugin.getStatsDashboardGUI()), null);

            // Configurações de Notificação
            registerCommand("notifications", "Configurar notificações",
                Arrays.asList("notif", "notificacoes", "alertas"), null,
                new NotificationSettingsCommand(plugin.getNotificationManager()), null);

            // Idioma
            dev.artix.artixduels.gui.LanguageSelectionGUI languageGUI = 
                new dev.artix.artixduels.gui.LanguageSelectionGUI(plugin.getLanguageManager());
            registerCommand("language", "Selecionar idioma",
                Arrays.asList("lang", "idioma", "idiomas"), null,
                new LanguageCommand(plugin.getLanguageManager(), languageGUI), null);

            // Editor de Arenas
            dev.artix.artixduels.gui.ArenaEditorGUI arenaEditorGUI = 
                new dev.artix.artixduels.gui.ArenaEditorGUI(plugin.getArenaEditor(), plugin.getArenaManager());
            registerCommand("arenaeditor", "Editor visual de arenas",
                Arrays.asList("arenaedit", "editarena", "editor"), "artixduels.admin",
                new ArenaEditorCommand(plugin.getArenaEditor(), arenaEditorGUI), null);

            // Editor de Kits
            dev.artix.artixduels.gui.KitEditorGUI kitEditorGUI = 
                new dev.artix.artixduels.gui.KitEditorGUI(plugin.getKitEditor(), plugin.getKitManager());
            registerCommand("kiteditor", "Editor visual de kits",
                Arrays.asList("kitedit", "editkit"), "artixduels.admin",
                new KitEditorCommand(plugin.getKitEditor(), kitEditorGUI), null);

            // Temas
            dev.artix.artixduels.gui.ThemeSelectionGUI themeGUI = 
                new dev.artix.artixduels.gui.ThemeSelectionGUI(plugin.getThemeManager());
            if (plugin.getMenuManager() != null) {
                themeGUI.setMenuManager(plugin.getMenuManager());
            }
            registerCommand("theme", "Gerenciar temas visuais",
                Arrays.asList("tema", "temas"), null,
                new ThemeCommand(plugin.getThemeManager(), themeGUI), null);

            // Conquistas
            dev.artix.artixduels.gui.AchievementGUI achievementGUI = 
                new dev.artix.artixduels.gui.AchievementGUI(plugin.getAchievementManager());
            registerCommand("achievements", "Ver conquistas",
                Arrays.asList("achievement", "conquistas", "conquista"), null,
                new AchievementCommand(plugin.getAchievementManager(), achievementGUI), null);

            // Títulos
            dev.artix.artixduels.gui.TitleGUI titleGUI = 
                new dev.artix.artixduels.gui.TitleGUI(plugin.getTitleManager());
            registerCommand("title", "Gerenciar títulos",
                Arrays.asList("titles", "titulo", "titulos"), null,
                new TitleCommand(plugin.getTitleManager(), titleGUI), null);

            // Dashboard Administrativo
            dev.artix.artixduels.gui.AdminDashboardGUI adminDashboardGUI = 
                new dev.artix.artixduels.gui.AdminDashboardGUI(plugin, plugin.getMetricsManager(), 
                    plugin.getDuelManager(), plugin.getStatsManager());
            registerCommand("admindashboard", "Dashboard administrativo",
                Arrays.asList("dashboard", "admin"), "artixduels.admin",
                new AdminDashboardCommand(adminDashboardGUI), null);

            // Anti-Cheat
            registerCommand("anticheat", "Gerenciar anti-cheat",
                Arrays.asList("ac", "antitrapaça"), "artixduels.admin",
                new AntiCheatCommand(plugin.getAntiCheatManager()), null);

            // Relatórios
            registerCommand("report", "Reportar jogador",
                Arrays.asList("reportar", "denunciar"), null,
                new ReportCommand(plugin.getReportManager()), null);
    }

    /**
     * Registra comandos administrativos (requerem permissão artixduels.admin).
     */
    private void registerAdminCommands() {
        // Comando administrativo principal
        registerCommand("dueladmin", "Comandos administrativos de duelos",
            Arrays.asList("dueladm", "dadm"), "artixduels.admin",
            new DuelAdminCommand(plugin, plugin.getDuelManager(), plugin.getKitManager(), 
                               plugin.getArenaManager(), plugin.getStatsManager(), 
                               plugin.getMessageManager(), plugin.getConfigGUI()),
            null);

        // Definir spawns
        registerCommand("setspawn", "Definir spawns do lobby e arenas",
            Arrays.asList("spawn"), "artixduels.admin",
            new SetSpawnCommand(plugin, plugin.getArenaManager()),
            new SetSpawnTabCompleter(plugin.getArenaManager()));

        // Gerenciar arenas
        registerCommand("arena", "Gerenciar arenas",
            Arrays.asList("arenas"), "artixduels.admin",
            new ArenaCommand(plugin.getArenaManager(), plugin.getKitManager()),
            new ArenaTabCompleter(plugin.getArenaManager(), plugin.getKitManager()));

        // Gerenciar kits
        registerCommand("kit", "Gerenciar kits",
            Arrays.asList("kits"), "artixduels.admin",
            new KitCommand(plugin.getKitManager(), plugin.getConfigGUI()),
            new KitTabCompleter(plugin.getKitManager()));

        // Gerenciar NPCs
        registerCommand("artix-npc", "Gerenciar NPCs de duelos",
            Arrays.asList("npc", "npcs", "anpc"), "artixduels.admin",
            new NPCCommand(plugin, plugin.getDuelNPC(), plugin.getMessageManager()),
            new NPCTabCompleter(plugin));

        // Gerenciar hologramas
        registerCommand("artix-holo", "Gerenciar hologramas",
            Arrays.asList("hologram", "holograms", "holo"), "artixduels.admin",
            new HologramCommand(plugin),
            new HologramTabCompleter(plugin));
    }
}

