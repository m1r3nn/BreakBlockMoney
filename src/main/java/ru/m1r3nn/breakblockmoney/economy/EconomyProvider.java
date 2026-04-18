package ru.m1r3nn.breakblockmoney.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public class EconomyProvider {

    private final Economy economy;

    private EconomyProvider(Economy economy) {
        this.economy = economy;
    }

    public static EconomyProvider setup(Logger logger) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault не найден, плагин отключается.");
            return null;
        }

        RegisteredServiceProvider<Economy> provider =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            logger.severe("Economy не найден. Установи EssentialsX или аналог.");
            return null;
        }

        return new EconomyProvider(provider.getProvider());
    }

    public void deposit(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }
}