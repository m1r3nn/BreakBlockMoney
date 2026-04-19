package ru.m1r3nn.breakblockmoney.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.service.RewardService;

public class MobListener implements Listener {

    private static final String SPAWNER_KEY = "bbm_from_spawner";

    private final PluginConfig pluginConfig;
    private final RewardService rewardService;

    public MobListener(PluginConfig pluginConfig, RewardService rewardService) {
        this.pluginConfig = pluginConfig;
        this.rewardService = rewardService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        if (killer.getGameMode() == GameMode.CREATIVE) return;
        if (pluginConfig.isWorldBlacklisted(entity.getWorld().getName())) return;
        if (pluginConfig.shouldIgnoreSpawnerMobs() && isFromSpawner(entity)) return;

        Double baseReward = pluginConfig.getMobReward(entity.getType());
        if (baseReward == null) return;

        rewardService.giveReward(killer, baseReward);
    }

    private boolean isFromSpawner(Entity entity) {
        return entity.hasMetadata(SPAWNER_KEY);
    }
}