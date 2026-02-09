package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.TrainingManager;
import dev.artix.artixduels.models.TrainingBot;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener para eventos de treinamento (bot = Citizens NPC com aparência de jogador).
 */
public class TrainingListener implements Listener {
    private final TrainingManager trainingManager;

    public TrainingListener(TrainingManager trainingManager) {
        this.trainingManager = trainingManager;
    }

    private static boolean isTrainingBot(Entity entity) {
        if (entity == null || !CitizensAPI.hasImplementation()) return false;
        if (!CitizensAPI.getNPCRegistry().isNPC(entity)) return false;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        return npc != null && npc.data().has("TrainingBot");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (isTrainingBot(damaged) && damager instanceof Player) {
            TrainingBot bot = trainingManager.getBotByEntity(damaged);
            if (bot != null) {
                Player player = (Player) damager;
                dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
                if (session != null && session.getBot().getBotId().equals(bot.getBotId())) {
                    double damage = event.getFinalDamage();
                    session.getStats().addPlayerDamageDealt(damage);
                    session.getStats().addBotDamageTaken(damage);
                    session.getStats().addPlayerHit();
                }
            }
        } else if (damaged instanceof Player && isTrainingBot(damager)) {
            Player player = (Player) damaged;
            TrainingBot bot = trainingManager.getBotByEntity(damager);
            if (bot != null) {
                dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
                if (session != null && session.getBot().getBotId().equals(bot.getBotId())) {
                    double damage = event.getFinalDamage();
                    session.getStats().addBotDamageDealt(damage);
                    session.getStats().addPlayerDamageTaken(damage);
                    session.getStats().addBotHit();
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (isTrainingBot(entity)) {
            event.getDrops().clear();
            event.setDroppedExp(0);

            TrainingBot bot = trainingManager.getBotByEntity(entity);
            if (bot != null) {
                trainingManager.handleBotDeath(bot);
            }
        } else if (entity instanceof Player) {
            Player player = (Player) entity;
            dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
            if (session != null) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                trainingManager.handlePlayerDeath(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dev.artix.artixduels.models.TrainingSession session = trainingManager.getSession(player.getUniqueId());
        if (session != null) {
            trainingManager.stopTraining(player.getUniqueId());
        }
    }
}
