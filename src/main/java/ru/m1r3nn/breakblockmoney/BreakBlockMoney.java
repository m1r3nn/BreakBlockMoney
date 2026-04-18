package ru.m1r3nn.breakblockmoney;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.m1r3nn.breakblockmoney.boost.BoostService;
import ru.m1r3nn.breakblockmoney.boost.BoostStorage;
import ru.m1r3nn.breakblockmoney.command.BreakBlockMoneyCommand;
import ru.m1r3nn.breakblockmoney.command.BreakBlockMoneyTabCompleter;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.economy.EconomyProvider;
import ru.m1r3nn.breakblockmoney.listener.BlockListener;

public class BreakBlockMoney extends JavaPlugin {

    private BoostStorage boostStorage;
    private BukkitTask cleanupTask;

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

        getServer().getPluginManager().registerEvents(
                new BlockListener(this, economyProvider, pluginConfig, boostService),
                this
        );

        BreakBlockMoneyCommand executor = new BreakBlockMoneyCommand(this, pluginConfig, boostStorage);
        getCommand("breakblockmoney").setExecutor(executor);
        getCommand("breakblockmoney").setTabCompleter(new BreakBlockMoneyTabCompleter(pluginConfig));

        cleanupTask = getServer().getScheduler().runTaskTimer(this, () -> {
            boostStorage.cleanupExpired();
            boostStorage.saveIfDirty();
        }, 20L * 60, 20L * 60);

        getLogger().info("BreakBlockMoney запущен.");
    }

    @Override
    public void onDisable() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }

        if (boostStorage != null) {
            boostStorage.cleanupExpired();
            boostStorage.save();
        }

        getLogger().info("BreakBlockMoney отключен.");
    }
}