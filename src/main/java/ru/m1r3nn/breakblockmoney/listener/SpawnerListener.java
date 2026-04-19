package ru.m1r3nn.breakblockmoney.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;

public class SpawnerListener implements Listener {

    private static final String SPAWNER_KEY = "bbm_from_spawner";

    private final Plugin plugin;
    private final PluginConfig pluginConfig;

    public SpawnerListener(Plugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!pluginConfig.shouldIgnoreSpawnerMobs()) return;
        event.getEntity().setMetadata(SPAWNER_KEY, new FixedMetadataValue(plugin, true));
    }
}