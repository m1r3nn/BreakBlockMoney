package ru.m1r3nn.breakblockmoney.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;

public class MessageConfig {

    private String rewardTemplate;
    private String rewardComboTemplate;
    private String reloadSuccessMessage;
    private String reloadFailMessage;
    private String noPermissionMessage;

    private String boostGivenMessage;
    private String boostGivenNotifyMessage;
    private String boostExpiredMessage;
    private String boostClearedMessage;
    private String boostClearedNotifyMessage;
    private String boostRemovedMessage;
    private String boostRemovedNotifyMessage;
    private String boostNotFoundMessage;
    private String noActiveBoostsMessage;

    private String invalidPlayerMessage;
    private String invalidBoostMessage;
    private String invalidTimeMessage;
    private String usageBoostMessage;

    private String boostListHeader;
    private String boostListEntry;
    private String boostListEmpty;

    void load(FileConfiguration config) {
        this.rewardTemplate = config.getString("messages.reward", "&a+{amount}$");
        this.rewardComboTemplate = config.getString("messages.reward-combo", "&e+{amount}$ (x{multiplier} Комбо)");
        this.reloadSuccessMessage = colorize(config.getString("messages.reload-success", "&aКонфигурация перезагружена."));
        this.reloadFailMessage = colorize(config.getString("messages.reload-fail", "&cОшибка при перезагрузке."));
        this.noPermissionMessage = colorize(config.getString("messages.no-permission", "&cНедостаточно прав."));

        this.boostGivenMessage = config.getString("messages.boost-given", "&aИгроку &e{player} &aвыдан буст &e{boost} &aна &e{time}");
        this.boostGivenNotifyMessage = config.getString("messages.boost-given-notify", "&aТебе выдан временный буст &e{boost} &aна &e{time}");
        this.boostExpiredMessage = config.getString("messages.boost-expired", "&cТвой временный буст &e{boost} &cистёк.");
        this.boostClearedMessage = config.getString("messages.boost-cleared", "&aВсе бусты игрока &e{player} &aочищены.");
        this.boostClearedNotifyMessage = colorize(config.getString("messages.boost-cleared-notify", "&cВсе твои временные бусты были сброшены."));
        this.boostRemovedMessage = config.getString("messages.boost-removed", "&aБуст &e{boost} &aудалён у игрока &e{player}&a.");
        this.boostRemovedNotifyMessage = config.getString("messages.boost-removed-notify", "&cТвой временный буст &e{boost} &cбыл удалён.");
        this.boostNotFoundMessage = config.getString("messages.boost-not-found", "&cУ игрока &e{player} &cнет активного буста &e{boost}&c.");
        this.noActiveBoostsMessage = config.getString("messages.no-active-boosts", "&cУ игрока &e{player} &cнет активных бустов.");

        this.invalidPlayerMessage = config.getString("messages.invalid-player", "&cИгрок &e{player} &cне найден.");
        this.invalidBoostMessage = config.getString("messages.invalid-boost", "&cБуст &e{boost} &cне найден в конфиге.");
        this.invalidTimeMessage = colorize(config.getString("messages.invalid-time", "&cНеверный формат времени. Используй: 1d2h30m"));
        this.usageBoostMessage = colorize(config.getString("messages.usage-boost", "&cИспользование: /breakblockmoney boost <игрок> <add/remove/clear/list> [буст] [время] [-s]"));

        this.boostListHeader = config.getString("messages.boost-list-header", "&6Активные бусты игрока &e{player}&6:");
        this.boostListEntry = config.getString("messages.boost-list-entry", "&7- &e{boost} &7(осталось &f{time}&7)");
        this.boostListEmpty = config.getString("messages.boost-list-empty", "&7У игрока &e{player} &7нет активных бустов.");
    }

    public String buildRewardMessage(double amount, DecimalFormat format) {
        return colorize(rewardTemplate.replace("{amount}", format.format(amount)));
    }

    public String buildRewardComboMessage(double amount, int multiplier, DecimalFormat format) {
        return colorize(rewardComboTemplate
                .replace("{amount}", format.format(amount))
                .replace("{multiplier}", String.valueOf(multiplier)));
    }

    public String buildBoostGivenMessage(String playerName, String boostName, String time) {
        return colorize(boostGivenMessage
                .replace("{player}", playerName)
                .replace("{boost}", boostName)
                .replace("{time}", time));
    }

    public String buildBoostGivenNotifyMessage(String boostName, String time) {
        return colorize(boostGivenNotifyMessage
                .replace("{boost}", boostName)
                .replace("{time}", time));
    }

    public String buildBoostExpiredMessage(String boostName) {
        return colorize(boostExpiredMessage.replace("{boost}", boostName));
    }

    public String buildBoostClearMessage(String playerName) {
        return colorize(boostClearedMessage.replace("{player}", playerName));
    }

    public String buildBoostRemovedMessage(String playerName, String boostName) {
        return colorize(boostRemovedMessage
                .replace("{player}", playerName)
                .replace("{boost}", boostName));
    }

    public String buildBoostRemovedNotifyMessage(String boostName) {
        return colorize(boostRemovedNotifyMessage.replace("{boost}", boostName));
    }

    public String buildBoostNotFoundMessage(String playerName, String boostName) {
        return colorize(boostNotFoundMessage
                .replace("{player}", playerName)
                .replace("{boost}", boostName));
    }

    public String buildInvalidPlayerMessage(String playerName) {
        return colorize(invalidPlayerMessage.replace("{player}", playerName));
    }

    public String buildInvalidBoostMessage(String boostName) {
        return colorize(invalidBoostMessage.replace("{boost}", boostName));
    }

    public String buildNoActiveBoostsMessage(String playerName) {
        return colorize(noActiveBoostsMessage.replace("{player}", playerName));
    }

    public String buildBoostListHeader(String playerName) {
        return colorize(boostListHeader.replace("{player}", playerName));
    }

    public String buildBoostListEntry(String boostName, String timeLeft) {
        return colorize(boostListEntry
                .replace("{boost}", boostName)
                .replace("{time}", timeLeft));
    }

    public String buildBoostListEmpty(String playerName) {
        return colorize(boostListEmpty.replace("{player}", playerName));
    }

    public String getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public String getReloadFailMessage() {
        return reloadFailMessage;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getBoostClearedNotifyMessage() {
        return boostClearedNotifyMessage;
    }

    public String getInvalidTimeMessage() {
        return invalidTimeMessage;
    }

    public String getUsageBoostMessage() {
        return usageBoostMessage;
    }

    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}