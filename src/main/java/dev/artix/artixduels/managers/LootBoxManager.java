package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import dev.artix.artixduels.models.LootBox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerenciador de loot boxes (carrega lootboxes.yml).
 */
public class LootBoxManager {
    private final ArtixDuels plugin;
    private final RewardManager rewardManager;
    private Map<String, LootBox> lootBoxes;
    private Map<UUID, List<String>> playerLootBoxes;

    public LootBoxManager(ArtixDuels plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
        this.lootBoxes = new HashMap<>();
        this.playerLootBoxes = new HashMap<>();

        loadLootBoxes();
        loadPlayerLootBoxes();
    }

    public void giveLootBox(UUID playerId, String lootBoxId) {
        playerLootBoxes.computeIfAbsent(playerId, k -> new ArrayList<>()).add(lootBoxId);
        savePlayerLootBoxes();
    }

    public void openLootBox(Player player, String lootBoxId) {
        LootBox lootBox = lootBoxes.get(lootBoxId);
        if (lootBox == null) {
            player.sendMessage("§cEsta loot box não está configurada no servidor.");
            return;
        }

        List<String> playerBoxes = playerLootBoxes.get(player.getUniqueId());
        if (playerBoxes == null || !playerBoxes.contains(lootBoxId)) {
            player.sendMessage("§cVocê não possui esta loot box!");
            return;
        }

        playerBoxes.remove(lootBoxId);

        animateOpening(player, lootBox);

        giveRewards(player, lootBox);

        savePlayerLootBoxes();
    }

    private void animateOpening(Player player, LootBox lootBox) {
        player.sendMessage("§6§l=== ABRINDO LOOT BOX ===");
        player.sendMessage(lootBox.getRarity().getDisplayName() + " " + lootBox.getName());

        for (int i = 3; i > 0; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§e" + count + "...");
            }, (3 - count) * 20L);
        }
    }

    private void giveRewards(Player player, LootBox lootBox) {
        if (lootBox.getRewards() == null || lootBox.getRewards().isEmpty()) return;

        Random random = new Random();
        for (LootBox.LootBoxReward reward : lootBox.getRewards()) {
            if (random.nextDouble() * 100 < reward.getChance()) {
                giveReward(player, reward);
            }
        }
    }

    private void giveReward(Player player, LootBox.LootBoxReward reward) {
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
                plugin.getLogger().warning("Material inválido na loot box: " + material);
            }
        }
    }

    public List<String> getPlayerLootBoxes(UUID playerId) {
        return new ArrayList<>(playerLootBoxes.getOrDefault(playerId, new ArrayList<>()));
    }

    /** Lista ids de caixas configuradas no servidor. */
    public Set<String> getRegisteredLootBoxIds() {
        return Collections.unmodifiableSet(new HashSet<>(lootBoxes.keySet()));
    }

    private void loadLootBoxes() {
        File lootBoxesFile = new File(plugin.getDataFolder(), "lootboxes.yml");
        if (!lootBoxesFile.exists()) {
            createDefaultLootBoxes(lootBoxesFile);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootBoxesFile);
        lootBoxes.clear();
        ConfigurationSection root = config.getConfigurationSection("lootboxes");
        if (root == null) {
            plugin.getLogger().warning("lootboxes.yml sem seção 'lootboxes'.");
            return;
        }

        for (String id : root.getKeys(false)) {
            try {
                String path = "lootboxes." + id;
                String name = config.getString(path + ".name", id);
                String rarityStr = config.getString(path + ".rarity", "COMMON");
                LootBox.LootBoxRarity rarity = LootBox.LootBoxRarity.valueOf(rarityStr.toUpperCase(Locale.ROOT));

                LootBox box = new LootBox(id, name, rarity);
                box.setDropChance(config.getDouble(path + ".drop-chance", 100.0));

                List<LootBox.LootBoxReward> rewards = new ArrayList<>();
                List<Map<?, ?>> rawList = castMapList(config.getList(path + ".rewards"));
                if (rawList != null) {
                    for (Map<?, ?> m : rawList) {
                        if (m == null) continue;
                        Object typeObj = m.get("type");
                        String type = typeObj != null ? String.valueOf(typeObj) : "money";
                        double chance = parseDouble(m.get("chance"), 100.0);
                        Map<String, Object> data = new HashMap<>();
                        for (Map.Entry<?, ?> e : m.entrySet()) {
                            String k = String.valueOf(e.getKey());
                            if ("type".equals(k) || "chance".equals(k)) continue;
                            data.put(k, e.getValue());
                        }
                        rewards.add(new LootBox.LootBoxReward(type, data, chance));
                    }
                }
                box.setRewards(rewards);
                lootBoxes.put(id, box);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Loot box ignorada '" + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Carregadas " + lootBoxes.size() + " loot box(es) de lootboxes.yml.");
    }

    private List<Map<?, ?>> castMapList(List<?> list) {
        if (list == null) return null;
        List<Map<?, ?>> out = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Map) {
                out.add((Map<?, ?>) o);
            }
        }
        return out.isEmpty() ? null : out;
    }

    private static double parseDouble(Object o, double def) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return def;
        }
    }

    private void createDefaultLootBoxes(File file) {
        try {
            YamlConfiguration config = new YamlConfiguration();

            List<Map<String, Object>> commonRewards = new ArrayList<>();
            Map<String, Object> r1 = new LinkedHashMap<>();
            r1.put("type", "money");
            r1.put("chance", 100.0);
            r1.put("amount", 50.0);
            commonRewards.add(r1);
            Map<String, Object> r2 = new LinkedHashMap<>();
            r2.put("type", "item");
            r2.put("chance", 40.0);
            r2.put("material", "GOLD_INGOT");
            r2.put("amount", 2);
            commonRewards.add(r2);

            config.set("lootboxes.common.name", "Caixa Comum");
            config.set("lootboxes.common.rarity", "COMMON");
            config.set("lootboxes.common.drop-chance", 100.0);
            config.set("lootboxes.common.rewards", commonRewards);

            List<Map<String, Object>> rareRewards = new ArrayList<>();
            Map<String, Object> rr1 = new LinkedHashMap<>();
            rr1.put("type", "money");
            rr1.put("chance", 100.0);
            rr1.put("amount", 150.0);
            rareRewards.add(rr1);
            Map<String, Object> rr2 = new LinkedHashMap<>();
            rr2.put("type", "item");
            rr2.put("chance", 50.0);
            rr2.put("material", "DIAMOND");
            rr2.put("amount", 1);
            rareRewards.add(rr2);

            config.set("lootboxes.rare.name", "Caixa Rara");
            config.set("lootboxes.rare.rarity", "RARE");
            config.set("lootboxes.rare.drop-chance", 100.0);
            config.set("lootboxes.rare.rewards", rareRewards);

            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao criar loot boxes: " + e.getMessage());
        }
    }

    private void loadPlayerLootBoxes() {
        File playerBoxesFile = new File(plugin.getDataFolder(), "player_lootboxes.yml");
        if (!playerBoxesFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerBoxesFile);
        if (config.contains("lootboxes")) {
            for (String playerIdStr : config.getConfigurationSection("lootboxes").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    List<String> boxes = config.getStringList("lootboxes." + playerIdStr);
                    playerLootBoxes.put(playerId, boxes != null ? new ArrayList<>(boxes) : new ArrayList<String>());
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }

    private void savePlayerLootBoxes() {
        File playerBoxesFile = new File(plugin.getDataFolder(), "player_lootboxes.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerBoxesFile);

        for (Map.Entry<UUID, List<String>> entry : playerLootBoxes.entrySet()) {
            config.set("lootboxes." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(playerBoxesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar loot boxes: " + e.getMessage());
        }
    }
}
