package net.anticrash;

import net.anticrash.listener.PlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiCrash extends JavaPlugin {

    public final String prefix = "§8» §c§lAntiCrash §7▶§8▶ §7";
    public final String no_permissions = prefix + "für diesen Befehl haben Sie §ckeine§7 Berechtigungen§8.";
    public final String permissions = "anticrash.";
    public static AntiCrash antiCrash;

    public void onEnable() {
        antiCrash = this;
        loadPlugin(antiCrash);
    }

    public void loadPlugin(AntiCrash antiCrash) {

        getCommand("anticrash").setExecutor(new net.anticrash.command.AntiCrash());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinEvent(), antiCrash);

        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(getPrefix() + "Status§8: §aAKTIVIERT");
        Bukkit.getConsoleSender().sendMessage(getPrefix() + "Version§8: §e" + antiCrash.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(getPrefix() + "Author§8: §eJavaInteger");
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNo_permissions() {
        return no_permissions;
    }

    public String getPermissions() {
        return permissions;
    }

    public static AntiCrash getAntiCrash() {
        return antiCrash;
    }
}
