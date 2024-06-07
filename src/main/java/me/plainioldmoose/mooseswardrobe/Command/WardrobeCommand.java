package me.plainioldmoose.mooseswardrobe.Command;

import me.plainioldmoose.mooseswardrobe.GUI.WardrobeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The WardrobeCommand class handles the execution of the wardrobe command.
 * When a player uses this command, it opens their wardrobe GUI.
 */
public class WardrobeCommand implements CommandExecutor {

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
            new WardrobeGUI().displayTo(player);
        }

        return true; // Indicate that the command was successfully executed
    }
}
