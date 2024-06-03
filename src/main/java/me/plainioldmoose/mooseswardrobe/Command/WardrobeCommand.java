package me.plainioldmoose.mooseswardrobe.Command;

import me.plainioldmoose.mooseswardrobe.GUI.WardrobeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WardrobeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");

            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new WardrobeGUI().displayTo(player);
        }
        return true;
    }
}
