package ru.m1r3nn.breakblockmoney;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.m1r3nn.breakblockmoney.boost.BoostExpiryTask;
import ru.m1r3nn.breakblockmoney.boost.BoostService;
import ru.m1r3nn.breakblockmoney.boost.BoostStorage;
import ru.m1r3nn.breakblockmoney.command.BreakBlockMoneyCommand;
import ru.m1r3nn.breakblockmoney.command.BreakBlockMoneyTabCompleter;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.economy.EconomyProvider;
import ru.m1r3nn.breakblockmoney.listener.BlockListener;
import ru.m1r3nn.breakblockmoney.listener.FishingListener;
import ru.m1r3nn.breakblockmoney.listener.MobListener;
import ru.m1r3nn.breakblockmoney.listener.SpawnerListener;
import ru.m1r3nn.breakblockmoney.service.RewardService;

public class BreakBlockMoney extends JavaPlugin {

    private BoostStorage boostStorage;
    private BukkitTask expiryTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        EconomyProvider economyProvider = EconomyProvider.setup(getLogger());
        if (economyProvider == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginConfig pluginConfig = PluginConfig.load(getConfig(), getLogger());
        boostStorage = new BoostStorage(this);
        BoostService boostService = new BoostService(pluginConfig, boostStorage);
        RewardService rewardService = new RewardService(economyProvider, pluginConfig, boostService);

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListener(this, pluginConfig, rewardService), this);
        pluginManager.registerEvents(new MobListener(pluginConfig, rewardService), this);
        pluginManager.registerEvents(new FishingListener(pluginConfig, rewardService), this);
        pluginManager.registerEvents(new SpawnerListener(this, pluginConfig), this);

        BreakBlockMoneyCommand executor = new BreakBlockMoneyCommand(this, pluginConfig, boostStorage);
        getCommand("breakblockmoney").setExecutor(executor);
        getCommand("breakblockmoney").setTabCompleter(new BreakBlockMoneyTabCompleter(pluginConfig));

        expiryTask = getServer().getScheduler().runTaskTimer(this,
                new BoostExpiryTask(boostStorage, pluginConfig.messages()),
                20L * 30, 20L * 30
        );

        getLogger().info("BreakBlockMoney запущен.");
    }

    @Override
    public void onDisable() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }

        if (boostStorage != null) {
            boostStorage.cleanupExpired();
            boostStorage.save();
        }

        getLogger().info("BreakBlockMoney отключен.");
    }
}