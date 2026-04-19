package ru.m1r3nn.breakblockmoney.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.service.RewardService;

public class BlockListener implements Listener {

    private static final String PLACED_KEY = "bbm_placed";

    private final Plugin plugin;
    private final PluginConfig pluginConfig;
    private final RewardService rewardService;

    public BlockListener(Plugin plugin, PluginConfig pluginConfig, RewardService rewardService) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.rewardService = rewardService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        if (pluginConfig.isWorldBlacklisted(block.getWorld().getName())) return;
        if (pluginConfig.shouldIgnorePlacedBlocks() && block.hasMetadata(PLACED_KEY)) return;

        Material type = block.getType();
        Double baseReward = pluginConfig.getBlockReward(type);
        if (baseReward == null) return;

        rewardService.giveReward(player, baseReward);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!pluginConfig.shouldIgnorePlacedBlocks()) return;
        event.getBlock().setMetadata(PLACED_KEY, new FixedMetadataValue(plugin, true));
    }
}