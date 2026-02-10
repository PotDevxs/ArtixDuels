package dev.artix.artixduels;

import dev.artix.artixduels.commands.CommandRegistry;
import dev.artix.artixduels.database.DatabaseManager;
import dev.artix.artixduels.database.IDuelHistoryDAO;
import dev.artix.artixduels.database.IStatsDAO;
import dev.artix.artixduels.gui.ConfigGUI;
import dev.artix.artixduels.gui.DuelModeSelectionGUI;
import dev.artix.artixduels.gui.ProfileGUI;
import dev.artix.artixduels.gui.ScoreboardModeSelectionGUI;
import dev.artix.artixduels.listeners.DuelListener;
import dev.artix.artixduels.listeners.HologramListener;
import dev.artix.artixduels.listeners.LobbyProtectionListener;
import dev.artix.artixduels.listeners.NPCListener;
import dev.artix.artixduels.listeners.ProfileItemListener;
import dev.artix.artixduels.listeners.TablistListener;
import dev.artix.artixduels.managers.*;
import dev.artix.artixduels.managers.HologramSystemManager;
import dev.artix.artixduels.npcs.DuelNPC;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ArtixDuels extends JavaPlugin {

    private DatabaseManager databaseManager;
    private IStatsDAO statsDAO;
    private IDuelHistoryDAO historyDAO;
    private StatsManager statsManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private ScoreboardManager scoreboardManager;
    private RewardManager rewardManager;
    private BetManager betManager;
    private CooldownManager cooldownManager;
    private SpectatorManager spectatorManager;
    private DuelManager duelManager;
    private DuelNPC duelNPC;
    private TablistManager tablistManager;
    private MessageManager messageManager;
    private ConfigGUI configGUI;
    private DuelModeSelectionGUI duelModeSelectionGUI;
    private PlayerScoreboardPreferences scoreboardPreferences;
    private ScoreboardModeSelectionGUI scoreboardModeSelectionGUI;
    private ProfileItemListener profileItemListener;
    private HologramSystemManager hologramSystemManager;
    private MenuManager menuManager;
    private dev.artix.artixduels.utils.IntegrationManager integrationManager;
    private dev.artix.artixduels.managers.RankingManager rankingManager;
    private dev.artix.artixduels.managers.CombatAnalyzer combatAnalyzer;
    private dev.artix.artixduels.managers.NotificationManager notificationManager;
    private dev.artix.artixduels.managers.LanguageManager languageManager;
    private dev.artix.artixduels.managers.ArenaEditor arenaEditor;
    private dev.artix.artixduels.managers.KitEditor kitEditor;
    private dev.artix.artixduels.managers.ThemeManager themeManager;
    private dev.artix.artixduels.managers.AchievementManager achievementManager;
    private dev.artix.artixduels.managers.TitleManager titleManager;
    private dev.artix.artixduels.managers.SeasonManager seasonManager;
    private dev.artix.artixduels.managers.QualificationManager qualificationManager;
    private dev.artix.artixduels.managers.MetricsManager metricsManager;
    private dev.artix.artixduels.managers.LootBoxManager lootBoxManager;
    private dev.artix.artixduels.managers.BattlePassManager battlePassManager;
    private dev.artix.artixduels.managers.EventManager eventManager;
    private dev.artix.artixduels.managers.FestivalManager festivalManager;
    private dev.artix.artixduels.managers.AntiCheatManager antiCheatManager;
    private dev.artix.artixduels.managers.ReportManager reportManager;
    private dev.artix.artixduels.managers.ChallengeManager challengeManager;
    private dev.artix.artixduels.managers.CosmeticManager cosmeticManager;
    private dev.artix.artixduels.managers.TournamentManager tournamentManager;
    private dev.artix.artixduels.managers.ReplayManager replayManager;
    private dev.artix.artixduels.managers.TrainingManager trainingManager;
    private dev.artix.artixduels.gui.RankingGUI rankingGUI;
    private dev.artix.artixduels.gui.ChallengeGUI challengeGUI;
    private dev.artix.artixduels.gui.CosmeticGUI cosmeticGUI;
    private dev.artix.artixduels.gui.TournamentGUI tournamentGUI;
    private dev.artix.artixduels.gui.ReplayGUI replayGUI;
    private dev.artix.artixduels.gui.TrainingGUI trainingGUI;
    private dev.artix.artixduels.gui.StatsDashboardGUI statsDashboardGUI;
    private FileConfiguration scoreboardConfig;
    private File scoreboardFile;
    private FileConfiguration tablistConfig;
    private File tablistFile;
    private FileConfiguration npcsConfig;
    private File npcsFile;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration kitsConfig;
    private File kitsFile;
    private FileConfiguration menusConfig;
    private File menusFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadScoreboardConfig();
        loadTablistConfig();
        loadNPCsConfig();
        loadMessagesConfig();
        loadKitsConfig();
        loadMenusConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        statsDAO = databaseManager.getStatsDAO();
        historyDAO = databaseManager.getHistoryDAO();
        statsManager = new StatsManager(statsDAO);

        messageManager = new MessageManager(messagesConfig);

        kitManager = new KitManager(kitsConfig, kitsFile);
        kitEditor = new dev.artix.artixduels.managers.KitEditor(this, kitManager);
        themeManager = new dev.artix.artixduels.managers.ThemeManager(this);
        themeManager.loadCustomThemes();
        achievementManager = new dev.artix.artixduels.managers.AchievementManager(this, statsManager, rewardManager, notificationManager);
        titleManager = new dev.artix.artixduels.managers.TitleManager(this, statsManager, achievementManager);
        seasonManager = new dev.artix.artixduels.managers.SeasonManager(this, statsManager, rewardManager);
        qualificationManager = new dev.artix.artixduels.managers.QualificationManager(this, statsManager);
        metricsManager = new dev.artix.artixduels.managers.MetricsManager(this, duelManager, statsManager);
        lootBoxManager = new dev.artix.artixduels.managers.LootBoxManager(this, rewardManager);
        battlePassManager = new dev.artix.artixduels.managers.BattlePassManager(this, rewardManager);
        eventManager = new dev.artix.artixduels.managers.EventManager(this, notificationManager);
        festivalManager = new dev.artix.artixduels.managers.FestivalManager(this, rewardManager);
        arenaManager = new ArenaManager(getConfig(), new File(getDataFolder(), "config.yml"));
        arenaEditor = new dev.artix.artixduels.managers.ArenaEditor(this, arenaManager);
        
        rewardManager = new RewardManager(this, getConfig());
        betManager = new BetManager(this, getConfig());
        cooldownManager = new CooldownManager(getConfig());
        spectatorManager = new SpectatorManager(arenaManager);
        
        duelManager = new DuelManager(this, kitManager, arenaManager, statsManager,
                null, rewardManager, betManager, cooldownManager, spectatorManager, historyDAO);
        
        // Verificar e inicializar integrações antes de criar PlaceholderManager
        integrationManager = new dev.artix.artixduels.utils.IntegrationManager(this);
        
        // Inicializar sistemas de ranking, combate e notificações
        rankingManager = new dev.artix.artixduels.managers.RankingManager(this, statsDAO);
        combatAnalyzer = new dev.artix.artixduels.managers.CombatAnalyzer();
        notificationManager = new dev.artix.artixduels.managers.NotificationManager(this, duelManager);
        languageManager = new dev.artix.artixduels.managers.LanguageManager(this);
        languageManager.loadPlayerLanguages();
        challengeManager = new dev.artix.artixduels.managers.ChallengeManager(this, statsManager, rewardManager);
        cosmeticManager = new dev.artix.artixduels.managers.CosmeticManager(this, statsManager);
        tournamentManager = new dev.artix.artixduels.managers.TournamentManager(this, duelManager, statsManager);
        replayManager = new dev.artix.artixduels.managers.ReplayManager(this);
        trainingManager = new dev.artix.artixduels.managers.TrainingManager(this, kitManager, arenaManager);
        antiCheatManager = new dev.artix.artixduels.managers.AntiCheatManager(this, getConfig());
        reportManager = new dev.artix.artixduels.managers.ReportManager(this);
        
        // Integrar sistemas no DuelManager
        duelManager.setCombatAnalyzer(combatAnalyzer);
        duelManager.setNotificationManager(notificationManager);
        duelManager.setChallengeManager(challengeManager);
        duelManager.setCosmeticManager(cosmeticManager);
        duelManager.setTournamentManager(tournamentManager);
        duelManager.setReplayManager(replayManager);
        duelManager.setAchievementManager(achievementManager);
        duelManager.setTitleManager(titleManager);
        
        PlaceholderManager placeholderManager = new PlaceholderManager(this, duelManager, statsManager);
        scoreboardPreferences = new PlayerScoreboardPreferences(getDataFolder());
        scoreboardManager = new ScoreboardManager(statsManager, scoreboardConfig, placeholderManager, scoreboardPreferences);
        scoreboardManager.setDuelManager(duelManager);
        if (themeManager != null) {
            scoreboardManager.setThemeManager(themeManager);
        }
        duelManager.setScoreboardManager(scoreboardManager);

        tablistManager = new TablistManager(tablistConfig, statsManager, duelManager);

        menuManager = new MenuManager(this);

        rankingGUI = new dev.artix.artixduels.gui.RankingGUI(this, rankingManager, menuManager);
        getServer().getPluginManager().registerEvents(rankingGUI, this);
        
        challengeGUI = new dev.artix.artixduels.gui.ChallengeGUI(challengeManager);
        getServer().getPluginManager().registerEvents(challengeGUI, this);
        
        cosmeticGUI = new dev.artix.artixduels.gui.CosmeticGUI(cosmeticManager);
        getServer().getPluginManager().registerEvents(cosmeticGUI, this);
        
        tournamentGUI = new dev.artix.artixduels.gui.TournamentGUI(tournamentManager);
        getServer().getPluginManager().registerEvents(tournamentGUI, this);
        
        replayGUI = new dev.artix.artixduels.gui.ReplayGUI(replayManager);
        getServer().getPluginManager().registerEvents(replayGUI, this);
        
        trainingGUI = new dev.artix.artixduels.gui.TrainingGUI(trainingManager);
        getServer().getPluginManager().registerEvents(trainingGUI, this);
        
        statsDashboardGUI = new dev.artix.artixduels.gui.StatsDashboardGUI(statsManager, rankingManager);
        getServer().getPluginManager().registerEvents(statsDashboardGUI, this);
        
        dev.artix.artixduels.gui.NotificationSettingsGUI notificationSettingsGUI = 
            new dev.artix.artixduels.gui.NotificationSettingsGUI(notificationManager);
        getServer().getPluginManager().registerEvents(notificationSettingsGUI, this);
        
        dev.artix.artixduels.gui.LanguageSelectionGUI languageGUI = 
            new dev.artix.artixduels.gui.LanguageSelectionGUI(languageManager);
        getServer().getPluginManager().registerEvents(languageGUI, this);
        
        dev.artix.artixduels.gui.ThemeSelectionGUI themeGUI = 
            new dev.artix.artixduels.gui.ThemeSelectionGUI(themeManager);
        if (menuManager != null) {
            themeGUI.setMenuManager(menuManager);
        }
        getServer().getPluginManager().registerEvents(themeGUI, this);
        
        dev.artix.artixduels.gui.AchievementGUI achievementGUI = 
            new dev.artix.artixduels.gui.AchievementGUI(achievementManager);
        getServer().getPluginManager().registerEvents(achievementGUI, this);
        
        dev.artix.artixduels.gui.TitleGUI titleGUI = 
            new dev.artix.artixduels.gui.TitleGUI(titleManager);
        getServer().getPluginManager().registerEvents(titleGUI, this);
        
        dev.artix.artixduels.gui.AdminDashboardGUI adminDashboardGUI = 
            new dev.artix.artixduels.gui.AdminDashboardGUI(this, metricsManager, duelManager, statsManager);
        getServer().getPluginManager().registerEvents(adminDashboardGUI, this);

        configGUI = new ConfigGUI(this, kitManager, arenaManager, messageManager, menuManager);
        getServer().getPluginManager().registerEvents(configGUI, this);

        duelModeSelectionGUI = new DuelModeSelectionGUI(this, duelManager, kitManager, arenaManager, messageManager, menuManager);
        getServer().getPluginManager().registerEvents(duelModeSelectionGUI, this);

        scoreboardModeSelectionGUI = new ScoreboardModeSelectionGUI(scoreboardPreferences, messageManager, scoreboardManager, menuManager);
        getServer().getPluginManager().registerEvents(scoreboardModeSelectionGUI, this);

        ProfileGUI profileGUI = new ProfileGUI(statsManager, menuManager);
        getServer().getPluginManager().registerEvents(profileGUI, this);

        duelNPC = new DuelNPC(this, duelManager, kitManager, arenaManager, statsManager, placeholderManager);
        duelNPC.loadNPCs(npcsConfig);

        hologramSystemManager = new HologramSystemManager(this, statsManager, statsDAO);
        hologramSystemManager.loadHolograms();
        hologramSystemManager.startUpdateTask();

        startTablistUpdateTask();

        // Verificar e inicializar integrações
        integrationManager = new dev.artix.artixduels.utils.IntegrationManager(this);

        // Registrar comandos programaticamente
        CommandRegistry commandRegistry = new CommandRegistry(this);
        commandRegistry.registerAllCommands();

        profileItemListener = new ProfileItemListener(this, scoreboardModeSelectionGUI);
        profileItemListener.setDuelModeSelectionGUI(duelModeSelectionGUI);
        profileItemListener.setProfileGUI(profileGUI);
        profileItemListener.setDuelManager(duelManager);

        getServer().getPluginManager().registerEvents(new DuelListener(duelManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new TablistListener(tablistManager, this), this);
        getServer().getPluginManager().registerEvents(profileItemListener, this);
        getServer().getPluginManager().registerEvents(new LobbyProtectionListener(this, duelManager), this);
        getServer().getPluginManager().registerEvents(new HologramListener(this), this);
        getServer().getPluginManager().registerEvents(new dev.artix.artixduels.listeners.TrainingListener(trainingManager), this);
        getServer().getPluginManager().registerEvents(new dev.artix.artixduels.listeners.SoupListener(duelManager, scoreboardManager), this);
        dev.artix.artixduels.listeners.CombatListener combatListener = new dev.artix.artixduels.listeners.CombatListener(duelManager, combatAnalyzer);
        combatListener.setScoreboardManager(scoreboardManager);
        getServer().getPluginManager().registerEvents(combatListener, this);
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(duelNPC), this);
        }

        getLogger().info("ArtixDuels habilitado com sucesso!");
    }

    private void startTablistUpdateTask() {
        if (tablistManager == null || !tablistManager.isEnabled()) return;

        // Executar de forma síncrona para evitar problemas com reflection
        // O intervalo no config está em segundos, converter para ticks (1 segundo = 20 ticks)
        long intervalTicks = tablistManager.getUpdateInterval() * 20L;
        getServer().getScheduler().runTaskTimer(this, () -> {
            tablistManager.updateAllTablists();
        }, 20L, intervalTicks);
    }

    @Override
    public void onDisable() {
        if (languageManager != null) {
            languageManager.savePlayerLanguages();
        }
        if (scoreboardManager != null) {
            scoreboardManager.clearAllScoreboards();
        }
        if (hologramSystemManager != null) {
            hologramSystemManager.stopUpdateTask();
        }
        if (duelNPC != null) {
            duelNPC.removeAllNPCs();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("ArtixDuels desabilitado.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ProfileItemListener getProfileItemListener() {
        return profileItemListener;
    }

    public HologramSystemManager getHologramSystemManager() {
        return hologramSystemManager;
    }

    public DuelNPC getDuelNPC() {
        return duelNPC;
    }

    public DuelModeSelectionGUI getDuelModeSelectionGUI() {
        return duelModeSelectionGUI;
    }

    public ScoreboardModeSelectionGUI getScoreboardModeSelectionGUI() {
        return scoreboardModeSelectionGUI;
    }

    public ConfigGUI getConfigGUI() {
        return configGUI;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public IDuelHistoryDAO getHistoryDAO() {
        return historyDAO;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public dev.artix.artixduels.utils.IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public dev.artix.artixduels.managers.RankingManager getRankingManager() {
        return rankingManager;
    }

    public dev.artix.artixduels.managers.CombatAnalyzer getCombatAnalyzer() {
        return combatAnalyzer;
    }

    public dev.artix.artixduels.managers.NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public dev.artix.artixduels.gui.RankingGUI getRankingGUI() {
        return rankingGUI;
    }

    public dev.artix.artixduels.managers.ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public dev.artix.artixduels.gui.ChallengeGUI getChallengeGUI() {
        return challengeGUI;
    }

    public dev.artix.artixduels.managers.CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }

    public dev.artix.artixduels.gui.CosmeticGUI getCosmeticGUI() {
        return cosmeticGUI;
    }

    public dev.artix.artixduels.managers.TournamentManager getTournamentManager() {
        return tournamentManager;
    }

    public dev.artix.artixduels.gui.TournamentGUI getTournamentGUI() {
        return tournamentGUI;
    }

    public dev.artix.artixduels.managers.ReplayManager getReplayManager() {
        return replayManager;
    }

    public dev.artix.artixduels.gui.ReplayGUI getReplayGUI() {
        return replayGUI;
    }

    public dev.artix.artixduels.managers.TrainingManager getTrainingManager() {
        return trainingManager;
    }

    public dev.artix.artixduels.gui.TrainingGUI getTrainingGUI() {
        return trainingGUI;
    }

    public dev.artix.artixduels.gui.StatsDashboardGUI getStatsDashboardGUI() {
        return statsDashboardGUI;
    }

    public dev.artix.artixduels.managers.LanguageManager getLanguageManager() {
        return languageManager;
    }

    public dev.artix.artixduels.managers.ArenaEditor getArenaEditor() {
        return arenaEditor;
    }

    public dev.artix.artixduels.managers.KitEditor getKitEditor() {
        return kitEditor;
    }

    public dev.artix.artixduels.managers.ThemeManager getThemeManager() {
        return themeManager;
    }

    public dev.artix.artixduels.managers.AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public dev.artix.artixduels.managers.TitleManager getTitleManager() {
        return titleManager;
    }

    public dev.artix.artixduels.managers.SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public dev.artix.artixduels.managers.QualificationManager getQualificationManager() {
        return qualificationManager;
    }

    public dev.artix.artixduels.managers.MetricsManager getMetricsManager() {
        return metricsManager;
    }

    public dev.artix.artixduels.managers.LootBoxManager getLootBoxManager() {
        return lootBoxManager;
    }

    public dev.artix.artixduels.managers.BattlePassManager getBattlePassManager() {
        return battlePassManager;
    }

    public dev.artix.artixduels.managers.EventManager getEventManager() {
        return eventManager;
    }

    public dev.artix.artixduels.managers.FestivalManager getFestivalManager() {
        return festivalManager;
    }

    public dev.artix.artixduels.managers.AntiCheatManager getAntiCheatManager() {
        return antiCheatManager;
    }

    public dev.artix.artixduels.managers.ReportManager getReportManager() {
        return reportManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    private void loadMenusConfig() {
        menusFile = new File(getDataFolder(), "menus.yml");
        if (!menusFile.exists()) {
            saveResource("menus.yml", false);
        }
        menusConfig = YamlConfiguration.loadConfiguration(menusFile);
    }

    public FileConfiguration getMenusConfig() {
        if (menusConfig == null) {
            loadMenusConfig();
        }
        return menusConfig;
    }

    public void reloadMenuConfig() {
        if (menusFile == null) {
            menusFile = new File(getDataFolder(), "menus.yml");
        }
        menusConfig = YamlConfiguration.loadConfiguration(menusFile);
        if (menuManager != null) {
            menuManager.reloadMenuConfig();
        }
    }

    private void loadScoreboardConfig() {
        scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    public void reloadScoreboardConfig() {
        if (scoreboardFile == null) {
            scoreboardFile = new File(getDataFolder(), "scoreboard.yml");
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        if (scoreboardManager != null && duelManager != null && scoreboardPreferences != null) {
            PlaceholderManager placeholderManager = new PlaceholderManager(this, duelManager, statsManager);
            scoreboardManager.setDuelManager(duelManager);
            if (themeManager != null) {
                scoreboardManager.setThemeManager(themeManager);
            }
            scoreboardManager.reload(scoreboardConfig, placeholderManager);
            duelManager.setScoreboardManager(scoreboardManager);
        }
    }

    public FileConfiguration getScoreboardConfig() {
        if (scoreboardConfig == null) {
            loadScoreboardConfig();
        }
        return scoreboardConfig;
    }

    private void loadTablistConfig() {
        tablistFile = new File(getDataFolder(), "tablist.yml");
        if (!tablistFile.exists()) {
            saveResource("tablist.yml", false);
        }
        tablistConfig = YamlConfiguration.loadConfiguration(tablistFile);
    }

    public void reloadTablistConfig() {
        if (tablistFile == null) {
            tablistFile = new File(getDataFolder(), "tablist.yml");
        }
        tablistConfig = YamlConfiguration.loadConfiguration(tablistFile);
        if (tablistManager != null) {
            tablistManager = new TablistManager(tablistConfig, statsManager, duelManager);
            startTablistUpdateTask();
        }
    }

    public FileConfiguration getTablistConfig() {
        if (tablistConfig == null) {
            loadTablistConfig();
        }
        return tablistConfig;
    }

    public TablistManager getTablistManager() {
        return tablistManager;
    }

    private void loadNPCsConfig() {
        npcsFile = new File(getDataFolder(), "npcs.yml");
        if (!npcsFile.exists()) {
            saveResource("npcs.yml", false);
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
    }

    public void reloadNPCsConfig() {
        if (npcsFile == null) {
            npcsFile = new File(getDataFolder(), "npcs.yml");
        }
        npcsConfig = YamlConfiguration.loadConfiguration(npcsFile);
        if (duelNPC != null) {
            duelNPC.removeAllNPCs();
            duelNPC.loadNPCs(npcsConfig);
        }
    }

    public FileConfiguration getNPCsConfig() {
        if (npcsConfig == null) {
            loadNPCsConfig();
        }
        return npcsConfig;
    }

    private void loadMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadMessagesConfig() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        if (messageManager != null) {
            messageManager.reload(messagesConfig);
        }
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            loadMessagesConfig();
        }
        return messagesConfig;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private void loadKitsConfig() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            saveResource("kits.yml", false);
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }

    public void reloadKitsConfig() {
        if (kitsFile == null) {
            kitsFile = new File(getDataFolder(), "kits.yml");
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        if (kitManager != null) {
            kitManager = new KitManager(kitsConfig, kitsFile);
        }
    }

    public FileConfiguration getKitsConfig() {
        if (kitsConfig == null) {
            loadKitsConfig();
        }
        return kitsConfig;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (arenaManager != null) {
            arenaManager.reload(getConfig());
        }
        if (rewardManager != null) {
            rewardManager.reload(getConfig());
        }
        if (betManager != null) {
            betManager.reload(getConfig());
        }
        if (cooldownManager != null) {
            cooldownManager.reload(getConfig());
        }
        reloadScoreboardConfig();
        reloadNPCsConfig();
        reloadMessagesConfig();
        reloadKitsConfig();
        reloadTablistConfig();
        reloadMenuConfig();
        
        // Recarregar verificações de integrações
        if (integrationManager != null) {
            integrationManager.reload();
        }
        
        // Recarregar anti-cheat e relatórios
        if (antiCheatManager != null) {
            antiCheatManager.reloadConfig(getConfig());
        }
        if (reportManager != null) {
            reportManager.reloadConfig();
        }
    }

    public org.bukkit.Location getLobbySpawn() {
        String spawnString = getConfig().getString("lobby-spawn");
        if (spawnString == null) return null;
        
        String[] parts = spawnString.split(",");
        if (parts.length < 4) return null;
        
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
        float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;
        
        return new org.bukkit.Location(org.bukkit.Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
