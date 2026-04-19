package ru.m1r3nn.breakblockmoney.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.m1r3nn.breakblockmoney.config.PluginConfig;

import java.util.ArrayList;
import java.util.List;

public class BreakBlockMoneyTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "boost");
    private static final List<String> BOOST_ACTIONS = List.of("add", "remove", "clear", "list");
    private static final List<String> TIME_EXAMPLES = List.of("1h", "30m", "1d", "1d12h");

    private final PluginConfig pluginConfig;

    public BreakBlockMoneyTabCompleter(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("breakblockmoney.admin")) return List.of();

        if (args.length == 1) return filter(SUBCOMMANDS, args[0]);

        if (!args[0].equalsIgnoreCase("boost")) return List.of();

        return switch (args.length) {
            case 2 -> filterPlayers(args[1]);
            case 3 -> filter(BOOST_ACTIONS, args[2]);
            case 4 -> {
                String action = args[2].toLowerCase();
                if (action.equals("add") || action.equals("remove")) {
                    yield filter(new ArrayList<>(pluginConfig.getBoostNames()), args[3]);
                }
                yield List.of();
            }
            case 5 -> {
                if (args[2].equalsIgnoreCase("add")) {
                    yield filter(TIME_EXAMPLES, args[4]);
                }
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<String> filter(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }

    private List<String> filterPlayers(String input) {
        List<String> result = new ArrayList<>();
        String lower = input.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(lower)) {
                result.add(player.getName());
            }
        }
        return result;
    }
}