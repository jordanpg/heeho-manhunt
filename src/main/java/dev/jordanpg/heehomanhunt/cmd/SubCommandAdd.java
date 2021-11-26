package dev.jordanpg.heehomanhunt.cmd;


import dev.jordanpg.heehomanhunt.ManhuntManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub-command to modify roles
 */
public class SubCommandAdd implements SubCommand {
    private ManhuntManager manager;

    public SubCommandAdd(ManhuntManager manager)
    {
        this.manager = manager;
    }

    @Override
    public String getPermission() {
        return "heehomanhunt.manageroles";
    }

    @Override
    public String getCommand() {
        return "add";
    }

    @Override
    public boolean printHelp(@NotNull CommandSender sender) {
        sender.sendMessage("/hhmanhunt " + ChatColor.BOLD + "add " + ChatColor.GREEN + "<runner|hunter|spectator> <players...>");
        sender.sendMessage("Assign manhunt roles");

        return true;
    }

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull String[] args) {
        if(args.length < 2) return printHelp(sender);

        ManhuntManager.ManhuntRole role;
        switch(args[0])
        {
            case "runner":
                role = ManhuntManager.ManhuntRole.Runner;
                break;
            case "hunter":
                role = ManhuntManager.ManhuntRole.Hunter;
                break;
            case "spectator":
                role = ManhuntManager.ManhuntRole.Spectator;
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown role \"" + args[0] + ",\" accepted: runner, hunter, spectator");
                return true;
        }

        Player target;
        List<String> successes = new ArrayList<>();
        List<String> fails = new ArrayList<>();

        for(int i = 1; i < args.length; i++)
        {
            target = Bukkit.getPlayer(args[i]);
            if(target == null)
            {
                fails.add(args[i]);
                continue;
            }

            manager.setRole(target.getUniqueId(), role);
            successes.add(target.getName());
        }

        if(successes.size() > 0)
            sender.sendMessage(ChatColor.GREEN + "Added to " + role.toString() + "s: " + String.join(", ", successes));
        if(fails.size() > 0)
            sender.sendMessage(ChatColor.RED + "Could not find player(s): " + String.join(", ", fails));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        final Iterable<String> roles = Arrays.asList("runner", "hunter", "spectator");
        List<String> completions = new ArrayList<>();

        // Match role names
        if(args.length <= 1)
            return StringUtil.copyPartialMatches(args[0], roles, completions);

        // Match player names
        return Bukkit.matchPlayer(args[args.length - 1]).stream().map(Player::getName).toList();
    }
}
