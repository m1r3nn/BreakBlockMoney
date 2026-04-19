package ru.m1r3nn.breakblockmoney.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.service.RewardService;

public class FishingListener implements Listener {

    private final PluginConfig pluginConfig;
    private final RewardService rewardService;

    public FishingListener(PluginConfig pluginConfig, RewardService rewardService) {
        this.pluginConfig = pluginConfig;
        this.rewardService = rewardService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item caughtItem)) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (pluginConfig.isWorldBlacklisted(player.getWorld().getName())) return;

        ItemStack itemStack = caughtItem.getItemStack();
        Material type = itemStack.getType();

        Double baseReward = pluginConfig.getFishingReward(type);
        if (baseReward == null) return;

        rewardService.giveReward(player, baseReward);
    }
}