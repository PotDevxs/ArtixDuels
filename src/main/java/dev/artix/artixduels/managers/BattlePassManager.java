package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.BattlePass;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de passe de batalha (carrega/salva battlepass.yml).
 */
public class BattlePassManager {
    private final ArtixDuels plugin;
    private final RewardManager rewardManager;
    private BattlePass currentBattlePass;
    private Map<UUID, BattlePass.BattlePassProgress> playerProgress;

    public BattlePassManager(ArtixDuels plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.playerProgress = new HashMap<>();
        loadBattlePass();
    }

    public void addBattlePassXP(UUID playerId, int xp) {
        if (currentBattlePass == null || !currentBattlePass.isActive()) return;

        BattlePass.BattlePassProgress progress = playerProgress.computeIfAbsent(playerId,
            k -> new BattlePass.BattlePassProgress(playerId));

        int oldLevel = progress.getLevel();
        progress.addXp(xp);

        if (progress.getLevel() > oldLevel) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage("§6§l[PASSE DE BATALHA] §eVocê subiu para o nível " + progress.getLevel() + "!");
            }
        }

        saveBattlePass();
    }

    public void claimReward(Player player, int level) {
        if (currentBattlePass == null) {
            player.sendMessage("§cNão há passe de batalha ativo.");
            return;
        }

        BattlePass.BattlePassProgress progress = playerProgress.get(player.getUniqueId());
        if (progress == null || progress.getLevel() < level) {
            player.sendMessage("§cVocê ainda não alcançou este nível!");
            return;
        }

        if (progress.getClaimedRewards().getOrDefault(level, false)) {
            player.sendMessage("§cVocê já reivindicou esta recompensa!");
            return;
        }

        Map<Integer, BattlePass.BattlePassReward> free = currentBattlePass.getFreeRewards();
        if (free != null) {
            BattlePass.BattlePassReward reward = free.get(level);
            if (reward != null) {
                giveReward(player, reward);
            }
        }

        if (progress.isPremium()) {
            Map<Integer, BattlePass.BattlePassReward> premium = currentBattlePass.getPremiumRewards();
            if (premium != null) {
                BattlePass.BattlePassReward premiumReward = premium.get(level);
                if (premiumReward != null) {
                    giveReward(player, premiumReward);
                }
            }
        }

        progress.getClaimedRewards().put(level, true);
        saveBattlePass();
    }

    private void giveReward(Player player, BattlePass.BattlePassReward reward) {
        if (reward == null) return;
        String type = reward.getType();
        Map<String, Object> data = reward.getData();
        if (data == null) data = Collections.emptyMap();

        if ("money".equalsIgnoreCase(type)) {
            Object amt = data.get("amount");
            if (amt instanceof Number) {
                rewardManager.giveMoney(player, ((Number) amt).doubleValue());
                player.sendMessage("§a+$" + amt);
            }
        } else if ("item".equalsIgnoreCase(type)) {
            String material = String.valueOf(data.getOrDefault("material", "DIAMOND"));
            int amount = data.get("amount") instanceof Number ? ((Number) data.get("amount")).intValue() : 1;
            try {
                ItemStack item = new ItemStack(Material.valueOf(material.toUpperCase(Locale.ROOT)), amount);
                player.getInventory().addItem(item);
                player.sendMessage("§aRecebeu: §f" + amount + "x " + material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Material inválido no passe: " + material);
            }
        }
    }

    public BattlePass getCurrentBattlePass() {
        return currentBattlePass;
    }

    public BattlePass.BattlePassProgress getPlayerProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, k -> new BattlePass.BattlePassProgress(playerId));
    }

    private void loadBattlePass() {
        File battlePassFile = new File(plugin.getDataFolder(), "battlepass.yml");
        if (!battlePassFile.exists()) {
            createDefaultBattlePass();
            saveBattlePass();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(battlePassFile);
        if (!config.contains("current.id")) {
            createDefaultBattlePass();
            saveBattlePass();
            return;
        }

        String id = config.getString("current.id", "bp_1");
        String name = config.getString("current.name", "Passe de Batalha");
        long start = config.getLong("current.start-time");
        long end = config.getLong("current.end-time");
        int maxLevel = config.getInt("current.max-level", 100);

        if (end <= start) {
            createDefaultBattlePass();
            saveBattlePass();
            return;
        }

        currentBattlePass = new BattlePass(id, name, start, end, maxLevel);
        currentBattlePass.setFreeRewards(loadRewardMap(config, "free-rewards"));
        currentBattlePass.setPremiumRewards(loadRewardMap(config, "premium-rewards"));

        playerProgress.clear();
        ConfigurationSection progSec = config.getConfigurationSection("progress");
        if (progSec != null) {
            for (String uuidStr : progSec.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String base = "progress." + uuidStr + ".";
                    BattlePass.BattlePassProgress p = new BattlePass.BattlePassProgress(uuid);
                    p.setXp(config.getInt(base + "xp", 0));
                    p.setPremium(config.getBoolean(base + "premium", false));
                    ConfigurationSection claimedSec = config.getConfigurationSection(base + "claimed");
                    if (claimedSec != null) {
                        for (String levelKey : claimedSec.getKeys(false)) {
                            p.getClaimedRewards().put(Integer.parseInt(levelKey), claimedSec.getBoolean(levelKey));
                        }
                    }
                    p.syncLevelFromXp(maxLevel);
                    playerProgress.put(uuid, p);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private Map<Integer, BattlePass.BattlePassReward> loadRewardMap(YamlConfiguration config, String path) {
        Map<Integer, BattlePass.BattlePassReward> map = new HashMap<>();
        ConfigurationSection sec = config.getConfigurationSection(path);
        if (sec == null) return map;
        for (String key : sec.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                String type = sec.getString(key + ".type", "money");
                Map<String, Object> data = new HashMap<>();
                ConfigurationSection dataSec = sec.getConfigurationSection(key);
                if (dataSec != null) {
                    for (String dk : dataSec.getKeys(false)) {
                        if ("type".equals(dk)) continue;
                        data.put(dk, dataSec.get(dk));
                    }
                }
                map.put(level, new BattlePass.BattlePassReward(type, data));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private void createDefaultBattlePass() {
        long now = System.currentTimeMillis();
        long duration = 30L * 24L * 60L * 60L * 1000L;
        currentBattlePass = new BattlePass("bp_1", "Passe de Batalha 1", now, now + duration, 100);

        Map<Integer, BattlePass.BattlePassReward> free = new HashMap<>();
        for (int lv = 1; lv <= 10; lv++) {
            Map<String, Object> data = new HashMap<>();
            data.put("amount", 25.0 * lv);
            free.put(lv, new BattlePass.BattlePassReward("money", data));
        }
        currentBattlePass.setFreeRewards(free);
        currentBattlePass.setPremiumRewards(new HashMap<Integer, BattlePass.BattlePassReward>());
    }

    private void saveBattlePass() {
        File battlePassFile = new File(plugin.getDataFolder(), "battlepass.yml");
        YamlConfiguration config = new YamlConfiguration();

        if (currentBattlePass != null) {
            config.set("current.id", currentBattlePass.getId());
            config.set("current.name", currentBattlePass.getName());
            config.set("current.start-time", currentBattlePass.getStartTime());
            config.set("current.end-time", currentBattlePass.getEndTime());
            config.set("current.max-level", currentBattlePass.getMaxLevel());

            saveRewardMap(config, "free-rewards", currentBattlePass.getFreeRewards());
            saveRewardMap(config, "premium-rewards", currentBattlePass.getPremiumRewards());
        }

        for (Map.Entry<UUID, BattlePass.BattlePassProgress> e : playerProgress.entrySet()) {
            UUID id = e.getKey();
            BattlePass.BattlePassProgress p = e.getValue();
            String base = "progress." + id + ".";
            config.set(base + "xp", p.getXp());
            config.set(base + "level", p.getLevel());
            config.set(base + "premium", p.isPremium());
            for (Map.Entry<Integer, Boolean> c : p.getClaimedRewards().entrySet()) {
                config.set(base + "claimed." + c.getKey(), c.getValue());
            }
        }

        try {
            config.save(battlePassFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar battle pass: " + e.getMessage());
        }
    }

    private void saveRewardMap(YamlConfiguration config, String path, Map<Integer, BattlePass.BattlePassReward> rewards) {
        if (rewards == null) return;
        for (Map.Entry<Integer, BattlePass.BattlePassReward> e : rewards.entrySet()) {
            String base = path + "." + e.getKey() + ".";
            BattlePass.BattlePassReward r = e.getValue();
            config.set(base + "type", r.getType());
            if (r.getData() != null) {
                for (Map.Entry<String, Object> d : r.getData().entrySet()) {
                    config.set(base + d.getKey(), d.getValue());
                }
            }
        }
    }
}
