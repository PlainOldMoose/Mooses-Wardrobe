package me.plainioldmoose.mooseswardrobe.Command;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.GUI.WardrobeGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The WardrobeCommand class handles the execution of the wardrobe command.
 * When a player uses this command, it opens their wardrobe GUI.
 */
public final class WardrobeCommand implements CommandExecutor, TabCompleter {

    /**
     * Executes the given command, returning its success.
     *
     * @param sender  The source of the command
     * @param command The command that was executed
     * @param label   The alias of the command which was used
     * @param args    The arguments passed to the command
     * @return true if the command was successfully executed, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!"); // Inform that only players can use the command
            return true;
        }
        Player player = (Player) sender; // Cast the sender to a player

        // If no arguments are provided, display the wardrobe GUI to the player
        if (args.length == 0) {
            if (!player.hasPermission("wardrobe.open")) {
                player.sendMessage("§b§lWardrobe §8»§3 You do not have permission!");
                return true;
            }
            new WardrobeGUI().displayTo(player, true, null);
            return true; // Return true after displaying GUI
        }

        // If one argument is provided, check if it's for opening another player's wardrobe
        if (args.length == 1) {
            if (!player.hasPermission("wardrobe.admin")) {
                player.sendMessage("§b§lWardrobe §8»§3 You do not have permission!");
                return true;
            }
            String partialName = args[0].toLowerCase();
            List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());

            if (onlinePlayers.isEmpty()) {
                player.sendMessage("§b§lWardrobe §8»§3 No matching players found!");
                return true;
            }

            // Auto-complete with the first matching player name
            if (onlinePlayers.size() == 1) {
                Player playerToOpen = Bukkit.getPlayer(onlinePlayers.get(0));
                new WardrobeGUI().displayTo(player, false, playerToOpen.getUniqueId());
                player.sendMessage("§b§lWardrobe §8»§3 Opening " + Bukkit.getPlayer(onlinePlayers.get(0)).getName() + "'s wardrobe");
                return true; // Return true after displaying GUI
            }

            // Display all matching player names for manual selection
            player.sendMessage("§b§lWardrobe §8»§3 Matching players: " + String.join(", ", onlinePlayers));
            return true;
        }

        // If two arguments are provided and the first is "reset", reset the wardrobe data
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("wardrobe.admin")) {
                player.sendMessage("§b§lWardrobe §8»§3 You do not have permission!");
                return true;
            }
            Player playerToReset = Bukkit.getPlayer(args[1]);
            if (playerToReset == null) {
                player.sendMessage("§b§lWardrobe §8»§3 Player does not exist!");
                return true;
            }
            WardrobeData.getInstance().reset(playerToReset);
            player.sendMessage("§b§lWardrobe §8»§3 Successfully reset wardrobe data for " + playerToReset.getName() + ".");
            return true; // Return true after resetting data
        }

        // If none of the conditions match, show command usage (though ideally, this should never happen if command syntax is strict)
        player.sendMessage("§b§lWardrobe §8»§3 Invalid command usage.");
        return false;
    }

    /**
     * Provides tab completion for the wardrobe command.
     *
     * @param sender  The source of the command
     * @param command The command that was executed
     * @param label   The alias of the command which was used
     * @param args    The arguments passed to the command
     * @return A list of possible completions for the last argument, or null for all players
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Tab completion for the first argument (command options)
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("wardrobe.admin")) {
                completions.add("reset");
            }
            if (sender.hasPermission("wardrobe.open")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            }
            return completions.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("wardrobe.admin")) {
            // Tab completion for the second argument (player names for reset)
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(); // Return an empty list if no completions are found
    }

}

