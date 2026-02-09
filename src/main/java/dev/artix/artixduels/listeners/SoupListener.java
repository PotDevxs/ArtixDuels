package dev.artix.artixduels.listeners;

import dev.artix.artixduels.managers.DuelManager;
import dev.artix.artixduels.managers.ScoreboardManager;
import dev.artix.artixduels.models.Duel;
import dev.artix.artixduels.models.DuelMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Listener para rastrear consumo de sopas durante duelos no modo SOPA.
 */
public class SoupListener implements Listener {
    private final DuelManager duelManager;
    private final ScoreboardManager scoreboardManager;

    public SoupListener(DuelManager duelManager, ScoreboardManager scoreboardManager) {
        this.duelManager = duelManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Duel duel = duelManager.getPlayerDuel(player);

        if (duel == null || duel.getState() != Duel.DuelState.FIGHTING) {
            return;
        }

        DuelMode mode = duel.getMode();
        // Verificar se é um modo de sopa (SOUP ou SOUPRECRAFT)
        if (mode != DuelMode.SOUP && mode != DuelMode.SOUPRECRAFT) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        // Verificar se o item consumido é uma sopa
        Material material = item.getType();
        // No Bukkit 1.8.8, a sopa de cogumelo é MUSHROOM_SOUP
        if (material == Material.MUSHROOM_SOUP || 
            material == Material.RABBIT_STEW) {
            
            UUID playerId = player.getUniqueId();
            
            // Adicionar contador de sopas
            if (duel.getPlayer1Id().equals(playerId)) {
                duel.addPlayer1Sopa();
            } else if (duel.getPlayer2Id().equals(playerId)) {
                duel.addPlayer2Sopa();
            }
            
            // Atualizar streak (aumentar quando consome sopa)
            if (duel.getPlayer1Id().equals(playerId)) {
                duel.addPlayer1Streak();
            } else if (duel.getPlayer2Id().equals(playerId)) {
                duel.addPlayer2Streak();
            }
            
            // Atualizar scoreboard
            if (scoreboardManager != null) {
                Player opponent = org.bukkit.Bukkit.getPlayer(duel.getOpponent(playerId));
                if (opponent != null) {
                    scoreboardManager.updateDuelScoreboard(player, duel);
                    scoreboardManager.updateDuelScoreboard(opponent, duel);
                }
            }
        }
    }
}
