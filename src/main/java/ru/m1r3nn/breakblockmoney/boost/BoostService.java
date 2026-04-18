package ru.m1r3nn.breakblockmoney.boost;

import org.bukkit.entity.Player;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;

import java.util.List;
import java.util.Map;

public class BoostService {

    private static final String BOOST_PERMISSION_PREFIX = "breakblockmoney.boost.";

    private final PluginConfig pluginConfig;
    private final BoostStorage boostStorage;

    public BoostService(PluginConfig pluginConfig, BoostStorage boostStorage) {
        this.pluginConfig = pluginConfig;
        this.boostStorage = boostStorage;
    }

    public double getTotalMultiplier(Player player) {
        double permissionMultiplier = getPermissionMultiplier(player);
        double temporaryMultiplier = getTemporaryMultiplier(player);
        return permissionMultiplier * temporaryMultiplier;
    }

    private double getPermissionMultiplier(Player player) {
        double max = 1.0;
        for (Map.Entry<String, Double> entry : pluginConfig.getBoosts().entrySet()) {
            if (player.hasPermission(BOOST_PERMISSION_PREFIX + entry.getKey())) {
                max = Math.max(max, entry.getValue());
            }
        }
        return max;
    }

    private double getTemporaryMultiplier(Player player) {
        double max = 1.0;
        List<BoostEntry> activeBoosts = boostStorage.getActiveBoosts(player.getUniqueId());
        for (BoostEntry entry : activeBoosts) {
            Double multiplier = pluginConfig.getBoosts().get(entry.getBoostName().toLowerCase());
            if (multiplier != null) {
                max = Math.max(max, multiplier);
            }
        }
        return max;
    }

    public BoostStorage getBoostStorage() {
        return boostStorage;
    }
}