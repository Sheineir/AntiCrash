package net.anticrash.listener;

import net.anticrash.utils.NettyUtil;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinEvent implements Listener {

    @EventHandler
    public void onCall (org.bukkit.event.player.PlayerJoinEvent playerJoinEvent) {
        CraftPlayer craftPlayer = (CraftPlayer) playerJoinEvent.getPlayer();
        NettyUtil nettyUtil = new NettyUtil(craftPlayer);
        nettyUtil.startForPlayer();
    }

}
