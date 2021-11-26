package dev.jordanpg.heehomanhunt.cmd;

import dev.jordanpg.heehomanhunt.ManhuntManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandHHManhunt implements CommandExecutor, TabCompleter {
    private List<SubCommand> subcommands = new ArrayList<>();
    private ManhuntManager manager;

    public CommandHHManhunt(ManhuntManager manager) {
        this.manager = manager;

        addSubCommand(new SubCommandAdd(manager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        manager.getLogger().info(String.join(" ", args));

        // TODO: Print help when no args given
        if(args.length < 1) return false;

        // Check for matches to registered subcommands
        for(SubCommand cmd : subcommands) {
            manager.getLogger().info(cmd.getCommand() + " " + cmd.getPermission() + " " + String.valueOf(commandSender.hasPermission(cmd.getPermission())));

            if(cmd.getCommand().equals(args[0]))
            {
                // Check permissions for this sub-command
                if(cmd.getPermission() != "" && !commandSender.hasPermission(cmd.getPermission())) return false;

                // Get only arguments to the sub-command & run it
                return cmd.run(commandSender, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if(args.length == 0)
            return getSubCommandNames();

        if(args.length == 1)
            return StringUtil.copyPartialMatches(args[0], getSubCommandNames(), completions);

        SubCommand cmd = findSubCommand(args[0]);
        if(cmd == null) return null;

        return cmd.onTabComplete(commandSender, command, s, Arrays.copyOfRange(args, 1, args.length));
    }


    /**
     * @return A list of all registered sub-commands
     */
    public List<String> getSubCommandNames()
    {
        return subcommands.stream().map(SubCommand::getCommand).toList();
    }

    private SubCommand findSubCommand(String command)
    {
        return subcommands.stream()
                .filter(cmd -> cmd.getCommand().equals(command))
                .findFirst()
                .orElse(null);
    }

    private <T extends SubCommand> void addSubCommand(T command) {
        subcommands.add(command);
    }
}
