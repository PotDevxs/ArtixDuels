package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    private ArtixDuels plugin;
    private Map<UUID, DuelRequest> pendingRequests;
    private Map<UUID, Duel> activeDuels;
    private Map<UUID, Duel> playerDuels;
    private Map<UUID, ItemStack[]> savedInventories;
    private Map<UUID, ItemStack[]> savedArmor;
    private Map<UUID, Location> savedLocations;
    private Queue<UUID> matchmakingQueue;
    private Map<DuelMode, Queue<UUID>> matchmakingQueuesByMode;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private RewardManager rewardManager;
    private BetManager betManager;
    private CooldownManager cooldownManager;
    private SpectatorManager spectatorManager;
    private dev.artix.artixduels.database.IDuelHistoryDAO historyDAO;
    private dev.artix.artixduels.managers.CombatAnalyzer combatAnalyzer;
    private dev.artix.artixduels.managers.NotificationManager notificationManager;
    private dev.artix.artixduels.managers.ChallengeManager challengeManager;
    private dev.artix.artixduels.managers.CosmeticManager cosmeticManager;
    private dev.artix.artixduels.managers.TournamentManager tournamentManager;
    private dev.artix.artixduels.managers.ReplayManager replayManager;
    private dev.artix.artixduels.managers.AchievementManager achievementManager;
    private dev.artix.artixduels.managers.TitleManager titleManager;

    public DuelManager(ArtixDuels plugin, KitManager kitManager, ArenaManager arenaManager, StatsManager statsManager,
                       ScoreboardManager scoreboardManager, RewardManager rewardManager, BetManager betManager,
                       CooldownManager cooldownManager, SpectatorManager spectatorManager,
                       dev.artix.artixduels.database.IDuelHistoryDAO historyDAO) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.scoreboardManager = scoreboardManager;
        this.rewardManager = rewardManager;
        this.betManager = betManager;
        this.cooldownManager = cooldownManager;
        this.spectatorManager = spectatorManager;
        this.historyDAO = historyDAO;
        this.pendingRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.playerDuels = new HashMap<>();
        this.savedInventories = new HashMap<>();
        this.savedArmor = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.matchmakingQueue = new LinkedList<>();
        this.matchmakingQueuesByMode = new HashMap<>();
        for (DuelMode mode : DuelMode.values()) {
            matchmakingQueuesByMode.put(mode, new LinkedList<>());
        }
    }

    public void sendDuelRequest(Player challenger, Player target, String kitName, String arenaName) {
        sendDuelRequest(challenger, target, kitName, arenaName, DuelMode.BEDFIGHT);
    }

    public void sendDuelRequest(Player challenger, Player target, String kitName, String arenaName, DuelMode mode) {
        if (isInDuel(challenger) || isInDuel(target)) {
            challenger.sendMessage("§cUm dos jogadores já está em um duelo!");
            return;
        }

        if (cooldownManager.isOnRequestCooldown(challenger.getUniqueId())) {
            long remaining = cooldownManager.getRemainingRequestCooldown(challenger.getUniqueId());
            challenger.sendMessage("§cAguarde §e" + remaining + " §csegundos antes de enviar outro convite!");
            return;
        }

        DuelRequest request = new DuelRequest(challenger, target, kitName, arenaName, mode);
        pendingRequests.put(target.getUniqueId(), request);
        cooldownManager.setRequestCooldown(challenger.getUniqueId());

        challenger.sendMessage("§aConvite de duelo enviado para §e" + target.getName() + "§a!");
        target.sendMessage("§e" + challenger.getName() + " §adesafiou você para um duelo!");
        target.sendMessage("§7Use §a/accept §7para aceitar ou §c/deny §7para recusar.");

        // Notificar sobre o convite
        if (notificationManager != null) {
            notificationManager.notifyDuelRequest(target, request);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(target.getUniqueId()) && 
                pendingRequests.get(target.getUniqueId()).equals(request)) {
                pendingRequests.remove(target.getUniqueId());
                challenger.sendMessage("§cO convite de duelo expirou.");
            }
        }, 6000L);
    }

    public boolean hasPendingRequest(UUID playerId) {
        return pendingRequests.containsKey(playerId);
    }

    public void acceptDuelRequest(Player player) {
        DuelRequest request = pendingRequests.get(player.getUniqueId());
        if (request == null || request.isExpired()) {
            player.sendMessage("§cVocê não tem convites de duelo pendentes!");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger == null || !challenger.isOnline()) {
            player.sendMessage("§cO jogador que te desafiou não está mais online!");
            pendingRequests.remove(player.getUniqueId());
            return;
        }

        pendingRequests.remove(player.getUniqueId());
        startDuel(challenger, player, request.getKitName(), request.getArenaName(), request.getMode());
    }

    public void denyDuelRequest(Player player) {
        DuelRequest request = pendingRequests.remove(player.getUniqueId());
        if (request == null) {
            player.sendMessage("§cVocê não tem convites de duelo pendentes!");
            return;
        }

        Player challenger = Bukkit.getPlayer(request.getChallengerId());
        if (challenger != null && challenger.isOnline()) {
            challenger.sendMessage("§c" + player.getName() + " recusou seu convite de duelo.");
        }
        player.sendMessage("§cVocê recusou o convite de duelo.");
    }

    public void startDuel(Player player1, Player player2, String kitName, String arenaName, DuelMode mode) {
        if (isInDuel(player1) || isInDuel(player2)) {
            return;
        }

        Kit kit = kitManager.getKit(kitName);
        Arena arena = arenaManager.getArena(arenaName);

        if (kit == null) {
            player1.sendMessage("§cKit não encontrado!");
            player2.sendMessage("§cKit não encontrado!");
            return;
        }

        if (arena == null) {
            arena = arenaManager.getAvailableArena();
            if (arena == null) {
                player1.sendMessage("§cNenhuma arena disponível!");
                player2.sendMessage("§cNenhuma arena disponível!");
                return;
            }
        }

        if (arena.isInUse()) {
            player1.sendMessage("§cA arena está em uso!");
            player2.sendMessage("§cA arena está em uso!");
            return;
        }

        arena.setInUse(true);
        Duel duel = new Duel(player1, player2, kitName, arenaName, mode);
        UUID duelId = UUID.randomUUID();
        activeDuels.put(duelId, duel);
        playerDuels.put(player1.getUniqueId(), duel);
        playerDuels.put(player2.getUniqueId(), duel);

        // Iniciar rastreamento de combate
        if (combatAnalyzer != null) {
            combatAnalyzer.startTracking(duel);
        }

        // Iniciar gravação de replay
        if (replayManager != null) {
            replayManager.startRecording(duel);
        }

        savePlayerInventory(player1);
        savePlayerInventory(player2);
        savePlayerLocation(player1);
        savePlayerLocation(player2);

        teleportPlayers(player1, player2, arena);
        giveKit(player1, kit);
        giveKit(player2, kit);

        scoreboardManager.createDuelScoreboard(player1, player2, duel);
        
        // Notificar início do duelo
        if (notificationManager != null) {
            notificationManager.notifyDuelStart(duel);
        }
        
        startCountdown(duelId, player1, player2);
    }

    private void savePlayerInventory(Player player) {
        ItemStack[] live = player.getInventory().getContents();
        ItemStack[] snapshot = new ItemStack[36];
        for (int i = 0; i < live.length && i < snapshot.length; i++) {
            snapshot[i] = live[i] != null ? live[i].clone() : null;
        }
        savedInventories.put(player.getUniqueId(), snapshot);

        ItemStack[] arm = player.getInventory().getArmorContents();
        ItemStack[] armCopy = new ItemStack[arm.length];
        for (int i = 0; i < arm.length; i++) {
            armCopy[i] = arm[i] != null ? arm[i].clone() : null;
        }
        savedArmor.put(player.getUniqueId(), armCopy);
    }

    private void savePlayerLocation(Player player) {
        savedLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    private void teleportPlayers(Player player1, Player player2, Arena arena) {
        player1.teleport(arena.getPlayer1Spawn());
        player2.teleport(arena.getPlayer2Spawn());
    }

    private void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        ItemStack[] src = kit.getContents();
        ItemStack[] kitCopy = new ItemStack[36];
        for (int i = 0; i < src.length && i < kitCopy.length; i++) {
            kitCopy[i] = src[i] != null ? src[i].clone() : null;
        }
        player.getInventory().setContents(kitCopy);
        ItemStack[] ar = kit.getArmor();
        ItemStack[] armorCopy = new ItemStack[ar.length];
        for (int i = 0; i < ar.length; i++) {
            armorCopy[i] = ar[i] != null ? ar[i].clone() : null;
        }
        player.getInventory().setArmorContents(armorCopy);
        player.updateInventory();
    }

    private void startCountdown(UUID duelId, Player player1, Player player2) {
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    player1.sendMessage("§eDuelo começando em §c" + countdown + "§e...");
                    player2.sendMessage("§eDuelo começando em §c" + countdown + "§e...");
                    countdown--;
                } else {
                    Duel duel = activeDuels.get(duelId);
                    if (duel != null) {
                        duel.setState(Duel.DuelState.FIGHTING);
                        player1.sendMessage("§a§lFIGHT!");
                        player2.sendMessage("§a§lFIGHT!");
                        scoreboardManager.updateDuelScoreboard(player1, duel);
                        scoreboardManager.updateDuelScoreboard(player2, duel);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void endDuel(UUID winnerId, UUID loserId, boolean draw) {
        Duel duel = playerDuels.get(winnerId);
        if (duel == null) {
            duel = playerDuels.get(loserId);
        }
        if (duel == null) return;

        Player winner = Bukkit.getPlayer(winnerId);
        Player loser = Bukkit.getPlayer(loserId);

        long duration = System.currentTimeMillis() - duel.getStartTime();

        if (draw) {
            if (winner != null) {
                winner.sendMessage("§eEmpate!");
                scoreboardManager.removeScoreboard(winner);
                scoreboardManager.createLobbyScoreboard(winner);
            }
            if (loser != null) {
                loser.sendMessage("§eEmpate!");
                scoreboardManager.removeScoreboard(loser);
                scoreboardManager.createLobbyScoreboard(loser);
            }
            statsManager.updateDrawStats(winnerId, loserId, duel.getMode());
        } else {
            if (winner != null) {
                winner.sendMessage("§a§lVITÓRIA!");
                
                // Executar efeito de vitória
                if (cosmeticManager != null) {
                    cosmeticManager.playVictoryEffect(winner);
                }
                
                PlayerStats winnerStats = statsManager.getPlayerStats(winner);
                winnerStats.addXp(50);
                statsManager.savePlayerStats(winnerStats);
                scoreboardManager.removeScoreboard(winner);
                scoreboardManager.createLobbyScoreboard(winner);
                cooldownManager.setDuelCooldown(winnerId);
            }
            if (loser != null) {
                loser.sendMessage("§c§lDERROTA!");
                PlayerStats loserStats = statsManager.getPlayerStats(loser);
                loserStats.addXp(10);
                statsManager.savePlayerStats(loserStats);
                scoreboardManager.removeScoreboard(loser);
                scoreboardManager.createLobbyScoreboard(loser);
                cooldownManager.setDuelCooldown(loserId);
            }
            statsManager.updatePlayerStats(winnerId, loserId, duel.getMode());
            
            // Adicionar kill para o vencedor
            PlayerStats winnerStats = statsManager.getPlayerStats(winnerId, null);
            if (winnerStats != null) {
                winnerStats.addModeKill(duel.getMode());
                statsManager.savePlayerStats(winnerStats);
            }
            
            // Atualizar desafios
            if (challengeManager != null) {
                challengeManager.updateProgress(winnerId, dev.artix.artixduels.models.Challenge.ChallengeObjective.WIN_DUELS, 1, duel.getMode());
                challengeManager.updateProgress(winnerId, dev.artix.artixduels.models.Challenge.ChallengeObjective.WIN_DUELS_MODE, 1, duel.getMode());
                challengeManager.updateProgress(winnerId, dev.artix.artixduels.models.Challenge.ChallengeObjective.GET_KILLS, 1, duel.getMode());
                challengeManager.updateProgress(winnerId, dev.artix.artixduels.models.Challenge.ChallengeObjective.GET_KILLS_MODE, 1, duel.getMode());
                challengeManager.updateProgress(loserId, dev.artix.artixduels.models.Challenge.ChallengeObjective.PLAY_DUELS, 1, duel.getMode());
            }
            
            // Processar resultado de torneio se aplicável
            if (tournamentManager != null) {
                dev.artix.artixduels.models.Tournament tournament = tournamentManager.getPlayerTournament(winnerId);
                if (tournament != null && tournament.getState() == dev.artix.artixduels.models.Tournament.TournamentState.IN_PROGRESS) {
                    tournamentManager.processMatchResult(tournament.getId(), winnerId, loserId);
                }
            }
            
            betManager.processBetResult(winnerId, loserId);
        }

        // Parar rastreamento de combate
        if (combatAnalyzer != null) {
            combatAnalyzer.stopTracking(duel);
        }

        // Parar gravação de replay
        if (replayManager != null && !draw) {
            replayManager.stopRecording(duel, winnerId);
        }

        // Notificar fim do duelo
        if (notificationManager != null && !draw) {
            notificationManager.notifyDuelEnd(duel, winnerId);
        }

        saveDuelHistory(duel, winnerId, loserId, draw, duration);
        
        // Registrar métricas
        if (plugin != null) {
            dev.artix.artixduels.managers.MetricsManager metricsManager = plugin.getMetricsManager();
            if (metricsManager != null) {
                metricsManager.recordDuel(duel, duration);
            }
            
            // Atualizar temporada
            dev.artix.artixduels.managers.SeasonManager seasonManager = plugin.getSeasonManager();
            if (seasonManager != null && !draw) {
                seasonManager.updateSeasonStats(winnerId, true);
                seasonManager.updateSeasonStats(loserId, false);
            }
            
            // Adicionar XP do battle pass
            dev.artix.artixduels.managers.BattlePassManager battlePassManager = plugin.getBattlePassManager();
            if (battlePassManager != null && !draw) {
                battlePassManager.addBattlePassXP(winnerId, 10);
                battlePassManager.addBattlePassXP(loserId, 5);
            }
        }
        
        spectatorManager.removeAllSpectators(duel);
        restorePlayers(winner, loser);
        cleanupDuel(duel);

        // Recompensas em item só depois de restaurar o inventário do lobby (evita kit + itens duplicados)
        if (!draw && rewardManager != null) {
            if (winner != null && winner.isOnline()) {
                rewardManager.giveWinRewards(winner);
            }
            if (loser != null && loser.isOnline()) {
                rewardManager.giveLossRewards(loser);
            }
        }
    }

    private void saveDuelHistory(Duel duel, UUID winnerId, UUID loserId, boolean draw, long duration) {
        if (historyDAO == null) return;

        dev.artix.artixduels.models.DuelHistory history = new dev.artix.artixduels.models.DuelHistory();
        history.setPlayer1Id(duel.getPlayer1Id());
        history.setPlayer1Name(Bukkit.getOfflinePlayer(duel.getPlayer1Id()).getName());
        history.setPlayer2Id(duel.getPlayer2Id());
        history.setPlayer2Name(Bukkit.getOfflinePlayer(duel.getPlayer2Id()).getName());
        history.setWinnerId(draw ? null : winnerId);
        history.setKitName(duel.getKitName());
        history.setArenaName(duel.getArenaName());
        history.setTimestamp(System.currentTimeMillis());
        history.setDuration(duration);

        historyDAO.saveDuelHistory(history);
    }

    private void restorePlayers(Player player1, Player player2) {
        if (player1 != null) {
            restorePlayer(player1);
        }
        if (player2 != null) {
            restorePlayer(player2);
        }
    }

    private void restorePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (savedInventories.containsKey(playerId)) {
            player.getInventory().setContents(savedInventories.get(playerId));
            savedInventories.remove(playerId);
        }
        if (savedArmor.containsKey(playerId)) {
            player.getInventory().setArmorContents(savedArmor.get(playerId));
            savedArmor.remove(playerId);
        }
        if (savedLocations.containsKey(playerId)) {
            player.teleport(savedLocations.get(playerId));
            savedLocations.remove(playerId);
        }
        
        org.bukkit.inventory.ItemStack profileItem = dev.artix.artixduels.listeners.ProfileItemListener.createProfileItem();
        org.bukkit.inventory.ItemStack queueItem = dev.artix.artixduels.listeners.ProfileItemListener.createQueueItem();
        org.bukkit.inventory.ItemStack challengeItem = dev.artix.artixduels.listeners.ProfileItemListener.createChallengeItem();
        
        player.getInventory().setItem(4, profileItem);
        player.getInventory().setItem(0, queueItem);
        player.getInventory().setItem(8, challengeItem);
        
        player.updateInventory();
    }

    private void cleanupDuel(Duel duel) {
        Arena arena = arenaManager.getArena(duel.getArenaName());
        if (arena != null) {
            arena.setInUse(false);
        }

        UUID duelId = null;
        for (Map.Entry<UUID, Duel> entry : activeDuels.entrySet()) {
            if (entry.getValue().equals(duel)) {
                duelId = entry.getKey();
                break;
            }
        }

        if (duelId != null) {
            activeDuels.remove(duelId);
        }
        playerDuels.remove(duel.getPlayer1Id());
        playerDuels.remove(duel.getPlayer2Id());
    }

    public boolean isInDuel(Player player) {
        return playerDuels.containsKey(player.getUniqueId());
    }

    public Duel getPlayerDuel(Player player) {
        return playerDuels.get(player.getUniqueId());
    }

    public void addToMatchmaking(Player player) {
        addToMatchmaking(player, DuelMode.BEDFIGHT);
    }

    public void addToMatchmaking(Player player, DuelMode mode) {
        if (isInDuel(player)) {
            player.sendMessage("§cVocê já está em um duelo!");
            return;
        }

        Queue<UUID> queue = matchmakingQueuesByMode.get(mode);
        if (queue == null) {
            queue = new LinkedList<>();
            matchmakingQueuesByMode.put(mode, queue);
        }

        if (queue.contains(player.getUniqueId())) {
            player.sendMessage("§cVocê já está na fila de matchmaking para " + mode.getDisplayName() + "!");
            return;
        }

        queue.add(player.getUniqueId());
        player.sendMessage("§aVocê entrou na fila de matchmaking para §e" + mode.getDisplayName() + "§a!");

        player.getInventory().clear();
        dev.artix.artixduels.listeners.ProfileItemListener.createLeaveQueueItem(player);

        if (scoreboardManager != null) {
            scoreboardManager.createQueueScoreboard(player, mode);
        }

        if (queue.size() >= 2) {
            UUID player1Id = queue.poll();
            UUID player2Id = queue.poll();
            Player player1 = Bukkit.getPlayer(player1Id);
            Player player2 = Bukkit.getPlayer(player2Id);

            if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                Arena arena = arenaManager.getAvailableArena();
                String defaultKit = kitManager.getKits().keySet().iterator().next();
                if (arena != null && defaultKit != null) {
                    startDuel(player1, player2, defaultKit, arena.getName(), mode);
                }
            }
        }
    }

    public void removeFromMatchmaking(Player player) {
        boolean wasInQueue = false;
        for (Queue<UUID> queue : matchmakingQueuesByMode.values()) {
            if (queue.remove(player.getUniqueId())) {
                wasInQueue = true;
                break;
            }
        }
        if (!wasInQueue && matchmakingQueue.remove(player.getUniqueId())) {
            wasInQueue = true;
        }
        
        if (wasInQueue) {
            player.sendMessage("§cVocê saiu da fila de matchmaking.");
            player.getInventory().clear();
            
            dev.artix.artixduels.listeners.ProfileItemListener profileItemListener = plugin.getProfileItemListener();
            if (profileItemListener != null) {
                profileItemListener.giveHotbarItems(player);
            }
            
            if (scoreboardManager != null) {
                scoreboardManager.createLobbyScoreboard(player);
            }
        }
    }

    public boolean isInQueue(Player player) {
        for (Queue<UUID> queue : matchmakingQueuesByMode.values()) {
            if (queue.contains(player.getUniqueId())) {
                return true;
            }
        }
        return matchmakingQueue.contains(player.getUniqueId());
    }

    public DuelMode getQueueMode(Player player) {
        for (Map.Entry<DuelMode, Queue<UUID>> entry : matchmakingQueuesByMode.entrySet()) {
            if (entry.getValue().contains(player.getUniqueId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public BetManager getBetManager() {
        return betManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public int getActiveDuelsCount() {
        return activeDuels.size();
    }

    public int getMatchmakingQueueSize() {
        return matchmakingQueue.size();
    }

    public int getActiveDuelsCountByMode(DuelMode mode) {
        int count = 0;
        for (Duel duel : activeDuels.values()) {
            if (duel.getMode() == mode) {
                count++;
            }
        }
        return count;
    }

    public int getMatchmakingQueueSizeByMode(DuelMode mode) {
        Queue<UUID> queue = matchmakingQueuesByMode.get(mode);
        return queue != null ? queue.size() : 0;
    }

    public void setScoreboardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public void setCombatAnalyzer(dev.artix.artixduels.managers.CombatAnalyzer combatAnalyzer) {
        this.combatAnalyzer = combatAnalyzer;
    }

    public void setNotificationManager(dev.artix.artixduels.managers.NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void setChallengeManager(dev.artix.artixduels.managers.ChallengeManager challengeManager) {
        this.challengeManager = challengeManager;
    }

    public void setCosmeticManager(dev.artix.artixduels.managers.CosmeticManager cosmeticManager) {
        this.cosmeticManager = cosmeticManager;
    }

    public void setTournamentManager(dev.artix.artixduels.managers.TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    public void setReplayManager(dev.artix.artixduels.managers.ReplayManager replayManager) {
        this.replayManager = replayManager;
    }

    public void setAchievementManager(dev.artix.artixduels.managers.AchievementManager achievementManager) {
        this.achievementManager = achievementManager;
    }

    public void setTitleManager(dev.artix.artixduels.managers.TitleManager titleManager) {
        this.titleManager = titleManager;
    }

    public ArtixDuels getPlugin() {
        return plugin;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}

