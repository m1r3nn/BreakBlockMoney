package ru.m1r3nn.breakblockmoney.listener;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import ru.m1r3nn.breakblockmoney.boost.BoostService;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.economy.EconomyProvider;

public class BlockListener implements Listener {

    private static final String PLACED_KEY = "bbm_placed";

    private final Plugin plugin;
    private final EconomyProvider economyProvider;
    private final PluginConfig pluginConfig;
    private final BoostService boostService;

    public BlockListener(Plugin plugin, EconomyProvider economyProvider, PluginConfig pluginConfig, BoostService boostService) {
        this.plugin = plugin;
        this.economyProvider = economyProvider;
        this.pluginConfig = pluginConfig;
        this.boostService = boostService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        if (pluginConfig.isWorldBlacklisted(block.getWorld().getName())) return;
        if (pluginConfig.shouldIgnorePlacedBlocks() && block.hasMetadata(PLACED_KEY)) return;

        Material type = block.getType();
        Double baseReward = pluginConfig.getReward(type);
        if (baseReward == null) return;

        double multiplier = boostService.getTotalMultiplier(player);
        boolean isCombo = pluginConfig.rollCombo();

        double finalReward = isCombo
                ? baseReward * multiplier * pluginConfig.getComboMultiplier()
                : baseReward * multiplier;

        economyProvider.deposit(player, finalReward);
        sendActionBar(player, isCombo, finalReward);
        playSound(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!pluginConfig.shouldIgnorePlacedBlocks()) return;
        event.getBlock().setMetadata(PLACED_KEY, new FixedMetadataValue(plugin, true));
    }

    private void sendActionBar(Player player, boolean isCombo, double amount) {
        String message = isCombo
                ? pluginConfig.messages().buildRewardComboMessage(amount, pluginConfig.getComboMultiplier(), pluginConfig.getAmountFormat())
                : pluginConfig.messages().buildRewardMessage(amount, pluginConfig.getAmountFormat());

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    private void playSound(Player player) {
        if (!pluginConfig.isSoundEnabled()) return;
        player.playSound(player.getLocation(), pluginConfig.getSound(), pluginConfig.getSoundVolume(), pluginConfig.getSoundPitch());
    }
}