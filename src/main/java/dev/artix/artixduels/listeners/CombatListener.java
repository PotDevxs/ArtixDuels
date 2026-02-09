package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.CombatAnalyzer;
import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.ScoreboardManager;
import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

/**
 * Listener para rastrear estatísticas de combate durante duelos.
 */
public class CombatListener implements Listener {
    private final DuelManager duelManager;
    private final CombatAnalyzer combatAnalyzer;
    private ScoreboardManager scoreboardManager;

    public CombatListener(DuelManager duelManager, CombatAnalyzer combatAnalyzer) {
        this.duelManager = duelManager;
        this.combatAnalyzer = combatAnalyzer;
    }

    public void setScoreboardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Duel duel = duelManager.getPlayerDuel(player);

        if (duel == null || duel.getState() != Duel.DuelState.FIGHTING) {
            return;
        }

        // Registrar dano recebido
        double damage = event.getFinalDamage();
        combatAnalyzer.recordDamageTaken(player.getUniqueId(), damage);

        // Se o dano foi causado por outro jogador
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            if (damageEvent.getDamager() instanceof Player) {
                Player damager = (Player) damageEvent.getDamager();
                Duel damagerDuel = duelManager.getPlayerDuel(damager);

                // Verificar se estão no mesmo duelo
                if (damagerDuel != null && damagerDuel.equals(duel)) {
                    // Registrar dano dado
                    combatAnalyzer.recordDamageDealt(damager.getUniqueId(), damage);
                    // Registrar hit
                    combatAnalyzer.recordHit(damager.getUniqueId());
                    combatAnalyzer.recordHitTaken(player.getUniqueId());
                    
                    // Resetar streak do jogador que recebeu dano no modo SOPA
                    DuelMode mode = duel.getMode();
                    if (mode == DuelMode.SOUP || mode == DuelMode.SOUPRECRAFT) {
                        UUID playerId = player.getUniqueId();
                        if (duel.getPlayer1Id().equals(playerId)) {
                            duel.resetPlayer1Streak();
                        } else if (duel.getPlayer2Id().equals(playerId)) {
                            duel.resetPlayer2Streak();
                        }
                        
                        // Atualizar scoreboard
                        if (scoreboardManager != null) {
                            scoreboardManager.updateDuelScoreboard(player, duel);
                            scoreboardManager.updateDuelScoreboard(damager, duel);
                        }
                    }
                }
            }
        }
    }
}

