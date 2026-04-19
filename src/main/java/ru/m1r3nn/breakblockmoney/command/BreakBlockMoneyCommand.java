package ru.m1r3nn.breakblockmoney.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.m1r3nn.breakblockmoney.boost.BoostEntry;
import ru.m1r3nn.breakblockmoney.boost.BoostStorage;
import ru.m1r3nn.breakblockmoney.config.MessageConfig;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;
import ru.m1r3nn.breakblockmoney.util.TimeParser;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BreakBlockMoneyCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PluginConfig pluginConfig;
    private final BoostStorage boostStorage;

    public BreakBlockMoneyCommand(JavaPlugin plugin, PluginConfig pluginConfig, BoostStorage boostStorage) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.boostStorage = boostStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "boost" -> handleBoost(sender, args);
            default -> { return false; }
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        MessageConfig messages = pluginConfig.messages();
        try {
            plugin.reloadConfig();
            pluginConfig.reload(plugin.getConfig(), plugin.getLogger());
            sender.sendMessage(messages.getReloadSuccessMessage());
        } catch (Exception e) {
            sender.sendMessage(messages.getReloadFailMessage());
            plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
        }
    }

    private void handleBoost(CommandSender sender, String[] args) {
        MessageConfig messages = pluginConfig.messages();

        if (args.length < 3) {
            sender.sendMessage(messages.getUsageBoostMessage());
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUuid = target != null ? target.getUniqueId() : resolveOfflineUuid(targetName);

        if (targetUuid == null) {
            sender.sendMessage(messages.buildInvalidPlayerMessage(targetName));
            return;
        }

        boolean silent = hasSilentFlag(args);

        switch (args[2].toLowerCase()) {
            case "clear" -> handleBoostClear(sender, targetName, targetUuid, target, silent);
            case "remove" -> handleBoostRemove(sender, targetName, targetUuid, target, args, silent);
            case "add" -> handleBoostAdd(sender, targetName, targetUuid, target, args, silent);
            case "list" -> handleBoostList(sender, targetName, targetUuid);
            default -> sender.sendMessage(messages.getUsageBoostMessage());
        }
    }

    private void handleBoostClear(CommandSender sender, String name, UUID uuid, Player target, boolean silent) {
        MessageConfig messages = pluginConfig.messages();
        boolean had = boostStorage.clearBoosts(uuid);

        if (had) {
            sender.sendMessage(messages.buildBoostClearMessage(name));
            if (!silent && target != null && !target.equals(sender)) {
                target.sendMessage(messages.getBoostClearedNotifyMessage());
            }
        } else {
            sender.sendMessage(messages.buildNoActiveBoostsMessage(name));
        }
    }

    private void handleBoostRemove(CommandSender sender, String name, UUID uuid, Player target, String[] args, boolean silent) {
        MessageConfig messages = pluginConfig.messages();

        if (args.length < 4) {
            sender.sendMessage(messages.getUsageBoostMessage());
            return;
        }

        String boostName = args[3].toLowerCase();
        if (!pluginConfig.isBoostValid(boostName)) {
            sender.sendMessage(messages.buildInvalidBoostMessage(boostName));
            return;
        }

        boolean removed = boostStorage.removeBoost(uuid, boostName);
        if (removed) {
            sender.sendMessage(messages.buildBoostRemovedMessage(name, boostName));
            if (!silent && target != null && !target.equals(sender)) {
                target.sendMessage(messages.buildBoostRemovedNotifyMessage(boostName));
            }
        } else {
            sender.sendMessage(messages.buildBoostNotFoundMessage(name, boostName));
        }
    }

    private void handleBoostAdd(CommandSender sender, String name, UUID uuid, Player target, String[] args, boolean silent) {
        MessageConfig messages = pluginConfig.messages();

        if (args.length < 5) {
            sender.sendMessage(messages.getUsageBoostMessage());
            return;
        }

        String boostName = args[3].toLowerCase();
        String timeInput = args[4];

        if (!pluginConfig.isBoostValid(boostName)) {
            sender.sendMessage(messages.buildInvalidBoostMessage(boostName));
            return;
        }

        long durationMillis = TimeParser.parseMillis(timeInput);
        if (durationMillis <= 0) {
            sender.sendMessage(messages.getInvalidTimeMessage());
            return;
        }

        long expiresAt = System.currentTimeMillis() + durationMillis;
        boostStorage.addBoost(uuid, new BoostEntry(boostName, expiresAt));

        String formattedTime = TimeParser.format(durationMillis);
        sender.sendMessage(messages.buildBoostGivenMessage(name, boostName, formattedTime));
        if (!silent && target != null && !target.equals(sender)) {
            target.sendMessage(messages.buildBoostGivenMessage(name, boostName, formattedTime));
        }
    }

    private void handleBoostList(CommandSender sender, String name, UUID uuid) {
        MessageConfig messages = pluginConfig.messages();
        List<BoostEntry> activeBoosts = boostStorage.getActiveBoosts(uuid);

        if (activeBoosts.isEmpty()) {
            sender.sendMessage(messages.buildBoostListEmpty(name));
            return;
        }

        sender.sendMessage(messages.buildBoostListHeader(name));

        long now = System.currentTimeMillis();
        for (BoostEntry entry : activeBoosts) {
            long remaining = entry.getExpiresAt() - now;
            String timeLeft = TimeParser.format(Math.max(remaining, 0));
            sender.sendMessage(messages.buildBoostListEntry(entry.getBoostName(), timeLeft));
        }
    }

    private boolean hasSilentFlag(String[] args) {
        return Arrays.asList(args).contains("-s");
    }

    @SuppressWarnings("deprecation")
    private UUID resolveOfflineUuid(String name) {
        var offlinePlayer = Bukkit.getOfflinePlayer(name);
        return offlinePlayer.hasPlayedBefore() ? offlinePlayer.getUniqueId() : null;
    }
}