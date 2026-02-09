package dev.artix.artixduels.models;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Representa um bot de treinamento (Citizens NPC com aparência de jogador).
 */
public class TrainingBot {
    private UUID botId;
    private String botName;
    private BotDifficulty difficulty;
    private NPC npc;
    private Player target;
    private long lastAction;
    private int comboCount;
    private boolean isBlocking;
    private double health;
    private double maxHealth;

    public TrainingBot(String botName, BotDifficulty difficulty, Plugin plugin) {
        this.botId = UUID.randomUUID();
        this.botName = botName;
        this.difficulty = difficulty;
        this.lastAction = System.currentTimeMillis();
        this.comboCount = 0;
        this.isBlocking = false;
        this.health = 20.0;
        this.maxHealth = 20.0;
    }

    public UUID getBotId() {
        return botId;
    }

    public String getBotName() {
        return botName;
    }

    public BotDifficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Retorna a entidade viva do NPC (para saúde, localização, dano).
     */
    public LivingEntity getEntity() {
        if (npc == null || !npc.isSpawned()) return null;
        return npc.getEntity() instanceof LivingEntity ? (LivingEntity) npc.getEntity() : null;
    }

    public NPC getNPC() {
        return npc;
    }

    public void setNPC(NPC npc, Plugin plugin) {
        this.npc = npc;
        if (npc != null) {
            npc.data().set("TrainingBot", true);
            npc.data().set("BotId", botId.toString());
        }
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public long getLastAction() {
        return lastAction;
    }

    public void updateLastAction() {
        this.lastAction = System.currentTimeMillis();
    }

    public int getComboCount() {
        return comboCount;
    }

    public void incrementCombo() {
        this.comboCount++;
    }

    public void resetCombo() {
        this.comboCount = 0;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public void setBlocking(boolean blocking) {
        this.isBlocking = blocking;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
        LivingEntity entity = getEntity();
        if (entity != null) {
            entity.setHealth(Math.min(health, entity.getMaxHealth()));
        }
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        LivingEntity entity = getEntity();
        if (entity != null) {
            entity.setMaxHealth(maxHealth);
        }
    }

    public void remove() {
        if (npc != null) {
            npc.destroy();
            npc = null;
        }
    }
}
