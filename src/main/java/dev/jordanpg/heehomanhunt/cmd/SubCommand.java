package dev.jordanpg.heehomanhunt.cmd;

import dev.jordanpg.heehomanhunt.ManhuntManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;


/**
 * Sub-command interface, extend to create a sub-command.
 * Sub-commands are only given their own arguments,
 * e.g. "/hhmanhunt foo bar baz" will only pass "bar baz" to the "foo" sub-command
 * Extends TabCompleter to use autocompletion
 */
public interface SubCommand extends TabCompleter {
    boolean run(@NotNull CommandSender sender, @NotNull String[] args);
    String getPermission();
    String getCommand();
    boolean printHelp(@NotNull CommandSender sender);
}
