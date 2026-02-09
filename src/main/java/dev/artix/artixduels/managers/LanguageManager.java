package dev.artix.artixduels.managers;

import dev.artix.artixduels.ArtixDuels;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gerenciador de idiomas e traduções.
 */
public class LanguageManager {
    private final ArtixDuels plugin;
    private final Map<String, FileConfiguration> languages;
    private final Map<UUID, String> playerLanguages;
    private String defaultLanguage;
    private File languagesFolder;

    public LanguageManager(ArtixDuels plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        this.playerLanguages = new HashMap<>();
        this.defaultLanguage = "pt";
        this.languagesFolder = new File(plugin.getDataFolder(), "languages");
        
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }
        
        loadLanguages();
    }

    /**
     * Carrega todos os arquivos de idioma.
     */
    private void loadLanguages() {
        String[] supportedLanguages = {"pt", "en", "es", "fr", "de"};
        
        for (String langCode : supportedLanguages) {
            File langFile = new File(languagesFolder, langCode + ".yml");
            if (!langFile.exists()) {
                createDefaultLanguageFile(langFile, langCode);
            }
            
            FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            languages.put(langCode, langConfig);
        }
    }

    /**
     * Cria um arquivo de idioma padrão.
     */
    private void createDefaultLanguageFile(File file, String langCode) {
        try {
            file.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Carregar traduções padrão baseadas no idioma
            Map<String, String> translations = getDefaultTranslations(langCode);
            
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao criar arquivo de idioma " + langCode + ": " + e.getMessage());
        }
    }

    /**
     * Obtém traduções padrão para um idioma.
     */
    private Map<String, String> getDefaultTranslations(String langCode) {
        Map<String, String> translations = new HashMap<>();
        
        switch (langCode) {
            case "pt":
                translations.put("messages.prefix", "&b[ArtixDuels] &r");
                translations.put("messages.duel.request-sent", "&aConvite de duelo enviado para &e{target}!");
                translations.put("messages.duel.request-received", "&e{challenger} &adesafiou você para um duelo!");
                translations.put("messages.duel.request-accept-hint", "&7Use &a/accept &7para aceitar ou &c/deny &7para recusar.");
                translations.put("messages.duel.request-accepted", "&aVocê aceitou o convite de duelo!");
                translations.put("messages.duel.request-denied", "&cVocê recusou o convite de duelo.");
                translations.put("messages.error.player-not-found", "&cJogador não encontrado!");
                translations.put("messages.error.kit-not-found", "&cKit não encontrado!");
                translations.put("messages.error.arena-not-found", "&cArena não encontrada!");
                translations.put("gui.profile.title", "&6&lPerfil");
                translations.put("gui.dashboard.title", "&6&lDashboard");
                translations.put("gui.ranking.title", "&6&lRanking");
                break;
            case "en":
                translations.put("messages.prefix", "&b[ArtixDuels] &r");
                translations.put("messages.duel.request-sent", "&aDuel request sent to &e{target}!");
                translations.put("messages.duel.request-received", "&e{challenger} &achallenged you to a duel!");
                translations.put("messages.duel.request-accept-hint", "&7Use &a/accept &7to accept or &c/deny &7to decline.");
                translations.put("messages.duel.request-accepted", "&aYou accepted the duel request!");
                translations.put("messages.duel.request-denied", "&cYou declined the duel request.");
                translations.put("messages.error.player-not-found", "&cPlayer not found!");
                translations.put("messages.error.kit-not-found", "&cKit not found!");
                translations.put("messages.error.arena-not-found", "&cArena not found!");
                translations.put("gui.profile.title", "&6&lProfile");
                translations.put("gui.dashboard.title", "&6&lDashboard");
                translations.put("gui.ranking.title", "&6&lRanking");
                break;
            case "es":
                translations.put("messages.prefix", "&b[ArtixDuels] &r");
                translations.put("messages.duel.request-sent", "&aSolicitud de duelo enviada a &e{target}!");
                translations.put("messages.duel.request-received", "&e{challenger} &ate desafió a un duelo!");
                translations.put("messages.duel.request-accept-hint", "&7Usa &a/accept &7para aceptar o &c/deny &7para rechazar.");
                translations.put("messages.duel.request-accepted", "&aAceptaste la solicitud de duelo!");
                translations.put("messages.duel.request-denied", "&cRechazaste la solicitud de duelo.");
                translations.put("messages.error.player-not-found", "&c¡Jugador no encontrado!");
                translations.put("messages.error.kit-not-found", "&c¡Kit no encontrado!");
                translations.put("messages.error.arena-not-found", "&c¡Arena no encontrada!");
                translations.put("gui.profile.title", "&6&lPerfil");
                translations.put("gui.dashboard.title", "&6&lPanel");
                translations.put("gui.ranking.title", "&6&lClasificación");
                break;
            case "fr":
                translations.put("messages.prefix", "&b[ArtixDuels] &r");
                translations.put("messages.duel.request-sent", "&aDemande de duel envoyée à &e{target}!");
                translations.put("messages.duel.request-received", "&e{challenger} &avous a défié en duel!");
                translations.put("messages.duel.request-accept-hint", "&7Utilisez &a/accept &7pour accepter ou &c/deny &7pour refuser.");
                translations.put("messages.duel.request-accepted", "&aVous avez accepté la demande de duel!");
                translations.put("messages.duel.request-denied", "&cVous avez refusé la demande de duel.");
                translations.put("messages.error.player-not-found", "&cJoueur introuvable!");
                translations.put("messages.error.kit-not-found", "&cKit introuvable!");
                translations.put("messages.error.arena-not-found", "&cArène introuvable!");
                translations.put("gui.profile.title", "&6&lProfil");
                translations.put("gui.dashboard.title", "&6&lTableau de bord");
                translations.put("gui.ranking.title", "&6&lClassement");
                break;
            case "de":
                translations.put("messages.prefix", "&b[ArtixDuels] &r");
                translations.put("messages.duel.request-sent", "&aDuell-Anfrage an &e{target} gesendet!");
                translations.put("messages.duel.request-received", "&e{challenger} &ahat Sie zu einem Duell herausgefordert!");
                translations.put("messages.duel.request-accept-hint", "&7Verwenden Sie &a/accept &7zum Akzeptieren oder &c/deny &7zum Ablehnen.");
                translations.put("messages.duel.request-accepted", "&aSie haben die Duell-Anfrage akzeptiert!");
                translations.put("messages.duel.request-denied", "&cSie haben die Duell-Anfrage abgelehnt.");
                translations.put("messages.error.player-not-found", "&cSpieler nicht gefunden!");
                translations.put("messages.error.kit-not-found", "&cKit nicht gefunden!");
                translations.put("messages.error.arena-not-found", "&cArena nicht gefunden!");
                translations.put("gui.profile.title", "&6&lProfil");
                translations.put("gui.dashboard.title", "&6&lDashboard");
                translations.put("gui.ranking.title", "&6&lRangliste");
                break;
        }
        
        return translations;
    }

    /**
     * Define o idioma de um jogador.
     */
    public void setPlayerLanguage(UUID playerId, String languageCode) {
        if (languages.containsKey(languageCode)) {
            playerLanguages.put(playerId, languageCode);
        } else {
            playerLanguages.put(playerId, defaultLanguage);
        }
    }

    /**
     * Obtém o idioma de um jogador.
     */
    public String getPlayerLanguage(UUID playerId) {
        return playerLanguages.getOrDefault(playerId, defaultLanguage);
    }

    /**
     * Obtém uma mensagem traduzida para um jogador.
     */
    public String getMessage(Player player, String key) {
        return getMessage(player.getUniqueId(), key, new HashMap<>());
    }

    /**
     * Obtém uma mensagem traduzida para um jogador com placeholders.
     */
    public String getMessage(Player player, String key, Map<String, String> placeholders) {
        return getMessage(player.getUniqueId(), key, placeholders);
    }

    /**
     * Obtém uma mensagem traduzida para um UUID.
     */
    public String getMessage(UUID playerId, String key, Map<String, String> placeholders) {
        String langCode = getPlayerLanguage(playerId);
        FileConfiguration langConfig = languages.get(langCode);
        
        if (langConfig == null) {
            langConfig = languages.get(defaultLanguage);
        }
        
        String message = langConfig.getString(key, "");
        if (message.isEmpty()) {
            // Fallback para idioma padrão
            FileConfiguration defaultConfig = languages.get(defaultLanguage);
            message = defaultConfig.getString(key, "&cTranslation not found: " + key);
        }
        
        // Aplicar placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = plugin.getThemeManager();
            if (themeManager != null) {
                String themeColor = themeManager.getColor(playerId, "primary");
                message = message.replace("<theme>", themeColor);
            } else {
                message = message.replace("<theme>", "&f");
            }
        } catch (Exception e) {
            message = message.replace("<theme>", "&f");
        }
        
        // Aplicar códigos de cor
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }

    /**
     * Obtém uma mensagem traduzida sem prefixo.
     */
    public String getMessageNoPrefix(Player player, String key) {
        return getMessageNoPrefix(player.getUniqueId(), key, new HashMap<>());
    }

    /**
     * Obtém uma mensagem traduzida sem prefixo com placeholders.
     */
    public String getMessageNoPrefix(Player player, String key, Map<String, String> placeholders) {
        return getMessageNoPrefix(player.getUniqueId(), key, placeholders);
    }

    /**
     * Obtém uma mensagem traduzida sem prefixo.
     */
    public String getMessageNoPrefix(UUID playerId, String key, Map<String, String> placeholders) {
        String langCode = getPlayerLanguage(playerId);
        FileConfiguration langConfig = languages.get(langCode);
        
        if (langConfig == null) {
            langConfig = languages.get(defaultLanguage);
        }
        
        String message = langConfig.getString(key, "");
        if (message.isEmpty()) {
            FileConfiguration defaultConfig = languages.get(defaultLanguage);
            message = defaultConfig.getString(key, "&cTranslation not found: " + key);
        }
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Processar placeholder <theme> - cor primária do tema do jogador
        try {
            dev.artix.artixduels.managers.ThemeManager themeManager = plugin.getThemeManager();
            if (themeManager != null) {
                String themeColor = themeManager.getColor(playerId, "primary");
                message = message.replace("<theme>", themeColor);
            } else {
                message = message.replace("<theme>", "&f");
            }
        } catch (Exception e) {
            message = message.replace("<theme>", "&f");
        }
        
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }

    /**
     * Obtém o prefixo traduzido.
     */
    public String getPrefix(UUID playerId) {
        String langCode = getPlayerLanguage(playerId);
        FileConfiguration langConfig = languages.get(langCode);
        
        if (langConfig == null) {
            langConfig = languages.get(defaultLanguage);
        }
        
        String prefix = langConfig.getString("messages.prefix", "&b[ArtixDuels] &r");
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    /**
     * Obtém o nome do idioma.
     */
    public String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "pt":
                return "Português";
            case "en":
                return "English";
            case "es":
                return "Español";
            case "fr":
                return "Français";
            case "de":
                return "Deutsch";
            default:
                return languageCode;
        }
    }

    /**
     * Obtém todos os idiomas suportados.
     */
    public String[] getSupportedLanguages() {
        return new String[]{"pt", "en", "es", "fr", "de"};
    }

    /**
     * Recarrega os arquivos de idioma.
     */
    public void reload() {
        languages.clear();
        loadLanguages();
    }

    /**
     * Salva as preferências de idioma dos jogadores.
     */
    public void savePlayerLanguages() {
        File prefsFile = new File(plugin.getDataFolder(), "player_languages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);
        
        for (Map.Entry<UUID, String> entry : playerLanguages.entrySet()) {
            config.set("languages." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            config.save(prefsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar preferências de idioma: " + e.getMessage());
        }
    }

    /**
     * Carrega as preferências de idioma dos jogadores.
     */
    public void loadPlayerLanguages() {
        File prefsFile = new File(plugin.getDataFolder(), "player_languages.yml");
        if (!prefsFile.exists()) {
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(prefsFile);
        if (config.contains("languages")) {
            for (String key : config.getConfigurationSection("languages").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    String langCode = config.getString("languages." + key, defaultLanguage);
                    playerLanguages.put(playerId, langCode);
                } catch (IllegalArgumentException e) {
                    // Ignorar UUIDs inválidos
                }
            }
        }
    }
}

