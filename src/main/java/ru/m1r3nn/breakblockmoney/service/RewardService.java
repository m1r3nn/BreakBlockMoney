package ru.m1r3nn.breakblockmoney.service;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.m1r3nn.breakblockmoney.boost.BoostService;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.economy.EconomyProvider;

public class RewardService {

    private final EconomyProvider economyProvider;
    private final PluginConfig pluginConfig;
    private final BoostService boostService;

    public RewardService(EconomyProvider economyProvider, PluginConfig pluginConfig, BoostService boostService) {
        this.economyProvider = economyProvider;
        this.pluginConfig = pluginConfig;
        this.boostService = boostService;
    }

    public void giveReward(Player player, double baseReward) {
        double multiplier = boostService.getTotalMultiplier(player);
        boolean isCombo = pluginConfig.rollCombo();

        double finalReward = isCombo
                ? baseReward * multiplier * pluginConfig.getComboMultiplier()
                : baseReward * multiplier;

        economyProvider.deposit(player, finalReward);
        sendActionBar(player, isCombo, finalReward);
        playSound(player);
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