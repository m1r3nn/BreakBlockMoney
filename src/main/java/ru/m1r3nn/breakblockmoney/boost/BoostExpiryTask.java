package ru.m1r3nn.breakblockmoney.boost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.m1r3nn.breakblockmoney.config.MessageConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BoostExpiryTask implements Runnable {

    private final BoostStorage boostStorage;
    private final MessageConfig messageConfig;

    public BoostExpiryTask(BoostStorage boostStorage, MessageConfig messageConfig) {
        this.boostStorage = boostStorage;
        this.messageConfig = messageConfig;
    }

    @Override
    public void run() {
        Map<UUID, List<BoostEntry>> expiredByPlayer = collectExpired();
        notifyPlayers(expiredByPlayer);
        boostStorage.cleanupExpired();
        boostStorage.saveIfDirty();
    }

    private Map<UUID, List<BoostEntry>> collectExpired() {
        Map<UUID, List<BoostEntry>> result = new java.util.HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            List<BoostEntry> allBoosts = boostStorage.getAllBoosts(uuid);

            List<BoostEntry> expired = new ArrayList<>();
            for (BoostEntry entry : allBoosts) {
                if (entry.isExpired()) {
                    expired.add(entry);
                }
            }

            if (!expired.isEmpty()) {
                result.put(uuid, expired);
            }
        }

        return result;
    }

    private void notifyPlayers(Map<UUID, List<BoostEntry>> expiredByPlayer) {
        for (Map.Entry<UUID, List<BoostEntry>> entry : expiredByPlayer.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;

            for (BoostEntry boost : entry.getValue()) {
                player.sendMessage(messageConfig.buildBoostExpiredMessage(boost.getBoostName()));
            }
        }
    }
}