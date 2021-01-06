package net.anticrash.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AntiCrash implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.sendMessage(" ");
            player.sendMessage(net.anticrash.AntiCrash.getAntiCrash().getPrefix() + "Author§8: §cJavaInteger");
            player.sendMessage(net.anticrash.AntiCrash.getAntiCrash().getPrefix() + "Version§8: §c" + net.anticrash.AntiCrash.getAntiCrash().getDescription().getVersion());
            player.sendMessage(" ");
            player.sendTitle("§c§lAntiCrash"," ");
        } else {
            commandSender.sendMessage(" ");
            commandSender.sendMessage(net.anticrash.AntiCrash.getAntiCrash().getPrefix() + "Author§8: §cJavaInteger");
            commandSender.sendMessage(net.anticrash.AntiCrash.getAntiCrash().getPrefix() + "Version§8: §c" + net.anticrash.AntiCrash.getAntiCrash().getDescription().getVersion());
            commandSender.sendMessage(" ");
        }

        return false;
    }
}
