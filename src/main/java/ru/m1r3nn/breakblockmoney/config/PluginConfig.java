package ru.m1r3nn.breakblockmoney.config;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class PluginConfig {

    private Map<Material, Double> blockRewards;
    private Map<EntityType, Double> mobRewards;
    private Map<Material, Double> fishingRewards;
    private Map<String, Double> boosts;
    private Set<String> blacklistedWorlds;
    private final MessageConfig messageConfig = new MessageConfig();

    private boolean ignorePlacedBlocks;
    private boolean ignoreSpawnerMobs;
    private boolean soundEnabled;
    private Sound sound;
    private float soundVolume;
    private float soundPitch;
    private DecimalFormat amountFormat;
    private int comboChance;
    private int comboMultiplier;

    private PluginConfig() {
    }

    public static PluginConfig load(FileConfiguration config, Logger logger) {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.applyValues(config, logger);
        return pluginConfig;
    }

    public void reload(FileConfiguration config, Logger logger) {
        applyValues(config, logger);
    }

    private void applyValues(FileConfiguration config, Logger logger) {
        this.blockRewards = loadBlockRewards(config, logger);
        this.mobRewards = loadMobRewards(config, logger);
        this.fishingRewards = loadFishingRewards(config, logger);
        this.boosts = loadBoosts(config, logger);
        this.blacklistedWorlds = loadBlacklist(config);
        this.ignorePlacedBlocks = config.getBoolean("ignore-placed-blocks", true);
        this.ignoreSpawnerMobs = config.getBoolean("ignore-spawner-mobs", true);
        this.soundEnabled = config.getBoolean("sound.enabled", true);
        this.sound = loadSound(config, logger);
        this.soundVolume = (float) config.getDouble("sound.volume", 1.0);
        this.soundPitch = (float) config.getDouble("sound.pitch", 1.0);
        this.amountFormat = buildDecimalFormat(config);
        this.comboChance = config.getInt("combo.chance", 5);
        this.comboMultiplier = config.getInt("combo.multiplier", 30);
        this.messageConfig.load(config);
    }

    private static Map<Material, Double> loadBlockRewards(FileConfiguration config, Logger logger) {
        return loadMaterialSection(config, "rewards", logger);
    }

    private static Map<Material, Double> loadFishingRewards(FileConfiguration config, Logger logger) {
        return loadMaterialSection(config, "fishing-rewards", logger);
    }

    private static Map<Material, Double> loadMaterialSection(FileConfiguration config, String sectionName, Logger logger) {
        Map<Material, Double> rewards = new EnumMap<>(Material.class);
        ConfigurationSection section = config.getConfigurationSection(sectionName);

        if (section == null) return rewards;

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logger.warning("Неизвестный материал в " + sectionName + ": " + key);
                continue;
            }

            double amount = section.getDouble(key);
            if (amount <= 0) {
                logger.warning("Награда для " + key + " в " + sectionName + " <= 0, пропущена.");
                continue;
            }

            rewards.put(material, amount);
        }

        return rewards;
    }

    private static Map<EntityType, Double> loadMobRewards(FileConfiguration config, Logger logger) {
        Map<EntityType, Double> rewards = new EnumMap<>(EntityType.class);
        ConfigurationSection section = config.getConfigurationSection("mob-rewards");

        if (section == null) return rewards;

        for (String key : section.getKeys(false)) {
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Неизвестный тип моба в mob-rewards: " + key);
                continue;
            }

            double amount = section.getDouble(key);
            if (amount <= 0) {
                logger.warning("Награда для моба " + key + " <= 0, пропущена.");
                continue;
            }

            rewards.put(entityType, amount);
        }

        return rewards;
    }

    private static Map<String, Double> loadBoosts(FileConfiguration config, Logger logger) {
        Map<String, Double> boosts = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("boosts");

        if (section == null) return boosts;

        for (String key : section.getKeys(false)) {
            double multiplier = section.getDouble(key);
            if (multiplier <= 0) {
                logger.warning("Множитель буста " + key + " <= 0, пропущен.");
                continue;
            }
            boosts.put(key.toLowerCase(), multiplier);
        }

        return boosts;
    }

    private static Set<String> loadBlacklist(FileConfiguration config) {
        List<String> list = config.getStringList("worlds.blacklist");
        return list.isEmpty() ? Collections.emptySet() : new HashSet<>(list);
    }

    private static Sound loadSound(FileConfiguration config, Logger logger) {
        String soundName = config.getString("sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Неизвестный звук: " + soundName + ". Используется ENTITY_EXPERIENCE_ORB_PICKUP.");
            return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }

    private static DecimalFormat buildDecimalFormat(FileConfiguration config) {
        int decimalPlaces = config.getInt("amount-format.decimal-places", 2);
        boolean thousandsSeparator = config.getBoolean("amount-format.thousands-separator", true);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        StringBuilder pattern = new StringBuilder(thousandsSeparator ? "#,##0" : "#");
        if (decimalPlaces > 0) {
            pattern.append(".");
            pattern.append("0".repeat(decimalPlaces));
        }

        DecimalFormat format = new DecimalFormat(pattern.toString(), symbols);
        format.setGroupingUsed(thousandsSeparator);
        return format;
    }

    public boolean rollCombo() {
        return comboChance > 0 && Math.random() * 100 < comboChance;
    }

    public int getComboMultiplier() {
        return comboMultiplier;
    }

    public boolean isBoostValid(String boostName) {
        return boosts.containsKey(boostName.toLowerCase());
    }

    public Set<String> getBoostNames() {
        return Collections.unmodifiableSet(boosts.keySet());
    }

    public Map<String, Double> getBoosts() {
        return Collections.unmodifiableMap(boosts);
    }

    public Double getBlockReward(Material material) {
        return blockRewards.get(material);
    }

    public Double getMobReward(EntityType entityType) {
        return mobRewards.get(entityType);
    }

    public Double getFishingReward(Material material) {
        return fishingRewards.get(material);
    }

    public boolean isWorldBlacklisted(String worldName) {
        return blacklistedWorlds.contains(worldName);
    }

    public boolean shouldIgnorePlacedBlocks() {
        return ignorePlacedBlocks;
    }

    public boolean shouldIgnoreSpawnerMobs() {
        return ignoreSpawnerMobs;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public Sound getSound() {
        return sound;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public DecimalFormat getAmountFormat() {
        return amountFormat;
    }

    public MessageConfig messages() {
        return messageConfig;
    }
}