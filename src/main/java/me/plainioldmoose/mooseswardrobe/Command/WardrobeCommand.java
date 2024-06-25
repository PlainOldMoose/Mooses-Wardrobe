package me.plainioldmoose.mooseswardrobe.Command;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.GUI.WardrobeGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The WardrobeCommand class handles the execution of the wardrobe command.
 * When a player uses this command, it opens their wardrobe GUI.
 */
public final class WardrobeCommand implements CommandExecutor {

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
                player.sendMessage("§f[§c§lWardrobe§f]§c You do not have permission!");
                return true;
            }
            new WardrobeGUI().displayTo(player);
        } else if (args.length == 1) {
            if (!player.hasPermission("wardrobe.admin")) {
                player.sendMessage("§f[§c§lWardrobe§f]§c You do not have permission!");
                return true;
            }
            Player playerArg = Bukkit.getPlayer(args[0]);
            if (playerArg != null) {
                new WardrobeGUI().displayTo(playerArg);
                return true;
            }
            player.sendMessage("§f[§c§lWardrobe§f]§c Player does not exist!");
            return true;
        } else if (args.length == 2) {
            WardrobeData.getInstance().reset(Bukkit.getPlayer(args[1]));
        }

        return true; // Indicate that the command was successfully executed
    }

//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//
//
//        if (args.length == 1)
//            return Arrays.asList("name");
//
//        return new ArrayList<>(); // null = all player names
//    }
}
