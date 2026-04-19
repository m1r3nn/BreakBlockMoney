package ru.m1r3nn.breakblockmoney.boost;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BoostStorage {

    private final File file;
    private final Logger logger;
    private final Map<UUID, List<BoostEntry>> activeBoosts = new HashMap<>();
    private boolean dirty = false;

    public BoostStorage(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "boosts.yml");
        this.logger = plugin.getLogger();
        load();
    }

    public void addBoost(UUID uuid, BoostEntry entry) {
        activeBoosts.computeIfAbsent(uuid, k -> new ArrayList<>()).add(entry);
        markDirty();
    }

    public boolean removeBoost(UUID uuid, String boostName) {
        List<BoostEntry> entries = activeBoosts.get(uuid);
        if (entries == null) return false;

        boolean removed = entries.removeIf(e -> e.getBoostName().equalsIgnoreCase(boostName));
        if (entries.isEmpty()) activeBoosts.remove(uuid);
        if (removed) markDirty();
        return removed;
    }

    public boolean clearBoosts(UUID uuid) {
        boolean had = activeBoosts.remove(uuid) != null;
        if (had) markDirty();
        return had;
    }

    public List<BoostEntry> getActiveBoosts(UUID uuid) {
        List<BoostEntry> boosts = activeBoosts.get(uuid);
        if (boosts == null) return Collections.emptyList();

        return boosts.stream()
                .filter(entry -> !entry.isExpired())
                .collect(Collectors.toUnmodifiableList());
    }

    public List<BoostEntry> getAllBoosts(UUID uuid) {
        List<BoostEntry> boosts = activeBoosts.get(uuid);
        if (boosts == null) return Collections.emptyList();
        return List.copyOf(boosts);
    }

    public void cleanupExpired() {
        boolean changed = false;

        var iterator = activeBoosts.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            boolean removedAny = entry.getValue().removeIf(BoostEntry::isExpired);
            if (removedAny) changed = true;
            if (entry.getValue().isEmpty()) iterator.remove();
        }

        if (changed) markDirty();
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
            dirty = false;
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, List<BoostEntry>> playerEntry : activeBoosts.entrySet()) {
            String uuidStr = playerEntry.getKey().toString();
            List<BoostEntry> entries = playerEntry.getValue();

            for (int i = 0; i < entries.size(); i++) {
                BoostEntry entry = entries.get(i);
                config.set(uuidStr + "." + i + ".boost", entry.getBoostName());
                config.set(uuidStr + "." + i + ".expires", entry.getExpiresAt());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            logger.severe("Не удалось сохранить boosts.yml: " + e.getMessage());
        }
    }

    private void markDirty() {
        dirty = true;
    }

    private void load() {
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String uuidStr : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                logger.warning("Невалидный UUID в boosts.yml: " + uuidStr);
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(uuidStr);
            if (section == null) continue;

            List<BoostEntry> entries = new ArrayList<>();
            for (String key : section.getKeys(false)) {
                String path = uuidStr + "." + key;
                String boostName = config.getString(path + ".boost");
                long expiresAt = config.getLong(path + ".expires");

                if (boostName == null) continue;

                BoostEntry entry = new BoostEntry(boostName, expiresAt);
                if (!entry.isExpired()) {
                    entries.add(entry);
                }
            }

            if (!entries.isEmpty()) {
                activeBoosts.put(uuid, entries);
            }
        }
    }
}