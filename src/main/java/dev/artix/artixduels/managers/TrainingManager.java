package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Gerenciador de treinamento contra bots.
 */
public class TrainingManager {
    private final ArtixDuels plugin;
    private final KitManager kitManager;
    private final ArenaManager arenaManager;
    private Map<UUID, TrainingSession> activeSessions;
    private Map<UUID, TrainingBot> activeBots;
    private static final long BOT_ACTION_INTERVAL = 20L; // 1 segundo

    public TrainingManager(ArtixDuels plugin, KitManager kitManager, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.arenaManager = arenaManager;
        this.activeSessions = new HashMap<>();
        this.activeBots = new HashMap<>();
        
        startBotAITask();
    }

    /**
     * Inicia uma sessão de treinamento.
     */
    public TrainingSession startTraining(Player player, BotDifficulty difficulty, String kitName, String arenaName, DuelMode mode) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage("§cVocê já está em uma sessão de treinamento!");
            return null;
        }

        Kit kit = kitManager.getKit(kitName);
        Arena arena = arenaManager.getArena(arenaName);

        if (kit == null) {
            player.sendMessage("§cKit não encontrado!");
            return null;
        }

        if (arena == null) {
            arena = arenaManager.getAvailableArena();
            if (arena == null) {
                player.sendMessage("§cNenhuma arena disponível!");
                return null;
            }
        }

        if (arena.isInUse()) {
            player.sendMessage("§cA arena está em uso!");
            return null;
        }

        arena.setInUse(true);

        if (!CitizensAPI.hasImplementation()) {
            player.sendMessage("§cO plugin Citizens é necessário para o modo treino com bot!");
            arena.setInUse(false);
            return null;
        }

        String botName = "§7[Bot] §e" + difficulty.getDisplayName();
        String npcDisplayName = ChatColor.stripColor(botName);
        TrainingBot bot = new TrainingBot(botName, difficulty, plugin);

        Location spawnLocation = arena.getPlayer1Spawn();
        if (spawnLocation == null) {
            spawnLocation = arena.getPlayer2Spawn();
            if (spawnLocation == null) {
                player.sendMessage("§cArena não tem spawn configurado!");
                arena.setInUse(false);
                return null;
            }
        }

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcDisplayName);
        npc.data().set("TrainingBot", true);
        npc.data().set("BotId", bot.getBotId().toString());
        npc.spawn(spawnLocation);

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        if (skinTrait != null) {
            skinTrait.setSkinName("Steve");
        }

        bot.setNPC(npc, plugin);
        bot.setTarget(player);
        bot.setMaxHealth(20.0);
        bot.setHealth(20.0);

        activeBots.put(bot.getBotId(), bot);

        TrainingSession session = new TrainingSession(player.getUniqueId(), bot, kitName, arenaName, mode);
        activeSessions.put(player.getUniqueId(), session);

        Location playerSpawn = arena.getPlayer2Spawn();
        if (playerSpawn == null) {
            playerSpawn = arena.getPlayer1Spawn();
        }
        if (playerSpawn != null) {
            player.teleport(playerSpawn);
        }
        giveKit(player, kit);
        giveBotKit(npc, kit);

        player.sendMessage("§aTreinamento iniciado contra bot " + difficulty.getDisplayName() + "!");
        player.sendMessage("§7Use §e/training stop §7para parar o treinamento.");

        return session;
    }

    /**
     * Para uma sessão de treinamento.
     */
    public void stopTraining(UUID playerId) {
        TrainingSession session = activeSessions.remove(playerId);
        if (session == null) {
            return;
        }

        session.setActive(false);
        TrainingBot bot = session.getBot();
        if (bot != null) {
            bot.remove();
            activeBots.remove(bot.getBotId());
        }

        Arena arena = arenaManager.getArena(session.getArenaName());
        if (arena != null) {
            arena.setInUse(false);
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendMessage("§cTreinamento finalizado!");
            showTrainingResults(player, session);
        }
    }

    /**
     * Obtém a sessão de treinamento de um jogador.
     */
    public TrainingSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    /**
     * Obtém um bot por ID.
     */
    public TrainingBot getBot(UUID botId) {
        return activeBots.get(botId);
    }

    /**
     * Obtém um bot por entidade (Citizens NPC).
     */
    public TrainingBot getBotByEntity(Entity entity) {
        if (entity == null || !CitizensAPI.hasImplementation()) return null;
        if (!CitizensAPI.getNPCRegistry().isNPC(entity)) return null;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null || !npc.data().has("TrainingBot") || !npc.data().has("BotId")) return null;
        try {
            UUID botId = UUID.fromString(npc.data().get("BotId").toString());
            return activeBots.get(botId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Dá kit para o jogador.
     */
    private void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        
        if (kit.getContents() != null) {
            for (int i = 0; i < kit.getContents().length && i < 36; i++) {
                if (kit.getContents()[i] != null) {
                    player.getInventory().setItem(i, kit.getContents()[i]);
                }
            }
        }
        
        if (kit.getArmor() != null) {
            ItemStack[] armor = kit.getArmor();
            player.getInventory().setHelmet(armor[0]);
            player.getInventory().setChestplate(armor[1]);
            player.getInventory().setLeggings(armor[2]);
            player.getInventory().setBoots(armor[3]);
        }
        
        player.updateInventory();
    }

    /**
     * Dá kit para o bot (Citizens NPC - Equipment trait).
     */
    private void giveBotKit(NPC npc, Kit kit) {
        Equipment equipment = npc.getTrait(Equipment.class);
        if (equipment == null) return;

        equipment.set(Equipment.EquipmentSlot.HAND, null);
        equipment.set(Equipment.EquipmentSlot.HELMET, null);
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, null);
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, null);
        equipment.set(Equipment.EquipmentSlot.BOOTS, null);

        if (kit.getContents() != null && kit.getContents().length > 0) {
            for (ItemStack item : kit.getContents()) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    equipment.set(Equipment.EquipmentSlot.HAND, item.clone());
                    break;
                }
            }
        }

        if (kit.getArmor() != null) {
            ItemStack[] armor = kit.getArmor();
            if (armor[0] != null) equipment.set(Equipment.EquipmentSlot.HELMET, armor[0].clone());
            if (armor[1] != null) equipment.set(Equipment.EquipmentSlot.CHESTPLATE, armor[1].clone());
            if (armor[2] != null) equipment.set(Equipment.EquipmentSlot.LEGGINGS, armor[2].clone());
            if (armor[3] != null) equipment.set(Equipment.EquipmentSlot.BOOTS, armor[3].clone());
        }
    }

    /**
     * Mostra resultados do treinamento.
     */
    private void showTrainingResults(Player player, TrainingSession session) {
        TrainingSession.TrainingStats stats = session.getStats();
        
        player.sendMessage("§6§l=== RESULTADOS DO TREINAMENTO ===");
        player.sendMessage("§7Duração: §b" + formatDuration(session.getDuration()));
        player.sendMessage("§7Acertos: §a" + stats.getPlayerHits() + " §7/ §c" + stats.getBotHits());
        player.sendMessage("§7Precisão: §b" + String.format("%.1f", stats.getPlayerAccuracy()) + "%");
        player.sendMessage("§7Eliminações: §a" + stats.getPlayerKills() + " §7/ §c" + stats.getBotKills());
        player.sendMessage("§7Taxa de Vitória: §b" + String.format("%.1f", stats.getPlayerWinRate()) + "%");
        player.sendMessage("§7Combos: §e" + stats.getPlayerCombos());
        player.sendMessage("§7Dano Causado: §a" + String.format("%.1f", stats.getPlayerDamageDealt()));
        player.sendMessage("§7Dano Recebido: §c" + String.format("%.1f", stats.getPlayerDamageTaken()));
    }

    /**
     * Formata duração.
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Inicia tarefa de IA do bot.
     */
    private void startBotAITask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (TrainingBot bot : new ArrayList<>(activeBots.values())) {
                    LivingEntity entity = bot.getEntity();
                    if (entity == null || entity.isDead()) {
                        continue;
                    }

                    Player target = bot.getTarget();
                    if (target == null || !target.isOnline()) {
                        continue;
                    }

                    TrainingSession session = getSessionForBot(bot);
                    if (session == null || !session.isActive()) {
                        continue;
                    }

                    updateBotAI(bot, target, session);
                }
            }
        }.runTaskTimer(plugin, 0L, BOT_ACTION_INTERVAL);
    }

    /**
     * Atualiza IA do bot (Citizens NPC - movimento e ataque).
     */
    private void updateBotAI(TrainingBot bot, Player target, TrainingSession session) {
        LivingEntity entity = bot.getEntity();
        NPC npc = bot.getNPC();
        if (entity == null || entity.isDead() || npc == null || !npc.isSpawned()) {
            return;
        }

        Location botLoc = entity.getLocation();
        Location targetLoc = target.getLocation();
        double distance = botLoc.distance(targetLoc);

        BotDifficulty difficulty = bot.getDifficulty();
        Random random = new Random();

        if (distance > 20) {
            npc.teleport(targetLoc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else if (distance > 3) {
            if (random.nextDouble() < difficulty.getMovementChance()) {
                npc.getNavigator().setTarget(target, true);
            }
        } else {
            npc.getNavigator().cancelNavigation();
            if (random.nextDouble() < difficulty.getHitChance()) {
                target.damage(1.0, entity);
                session.getStats().addBotHit();
                bot.incrementCombo();

                if (bot.getComboCount() >= 3 && random.nextDouble() < difficulty.getComboChance()) {
                    session.getStats().addBotCombo();
                }
            }
        }

        if (random.nextDouble() < difficulty.getBlockChance() && distance < 2) {
            bot.setBlocking(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> bot.setBlocking(false), 20L);
        }

        bot.updateLastAction();
    }

    /**
     * Obtém sessão para um bot.
     */
    private TrainingSession getSessionForBot(TrainingBot bot) {
        for (TrainingSession session : activeSessions.values()) {
            if (session.getBot().getBotId().equals(bot.getBotId())) {
                return session;
            }
        }
        return null;
    }

    /**
     * Processa morte do bot (Citizens NPC - respawn após delay).
     */
    public void handleBotDeath(TrainingBot bot) {
        TrainingSession session = getSessionForBot(bot);
        if (session != null) {
            session.getStats().addPlayerKill();

            Player player = Bukkit.getPlayer(session.getPlayerId());
            if (player != null && player.isOnline()) {
                player.sendMessage("§aVocê eliminou o bot!");

                NPC npc = bot.getNPC();
                Location spawnLoc = arenaManager.getArena(session.getArenaName()).getPlayer1Spawn();
                if (npc != null && spawnLoc != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (npc.isSpawned()) {
                            npc.despawn();
                        }
                        npc.spawn(spawnLoc);
                        bot.setHealth(bot.getMaxHealth());
                        LivingEntity ent = bot.getEntity();
                        if (ent != null) {
                            ent.setHealth(ent.getMaxHealth());
                        }
                    }, 60L);
                }
            }
        }
    }

    /**
     * Processa morte do jogador.
     */
    public void handlePlayerDeath(Player player) {
        TrainingSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.getStats().addBotKill();
            
            player.sendMessage("§cVocê foi eliminado pelo bot!");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                Location spawnLoc = arenaManager.getArena(session.getArenaName()).getPlayer2Spawn();
                if (spawnLoc != null) {
                    player.teleport(spawnLoc);
                }
                giveKit(player, kitManager.getKit(session.getKitName()));
            }, 60L);
        }
    }
}

