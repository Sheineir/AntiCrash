package net.anticrash.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.anticrash.AntiCrash;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.io.IOException;
import java.util.List;

public class NettyUtil {

    private final CraftPlayer craftPlayer;


    public NettyUtil (CraftPlayer craftPlayer) {
        this.craftPlayer = craftPlayer;
    }

    /*
    startForPlayer - Injects the new decoder for the specific player.
    This method has been made by me.
     */

    public void startForPlayer() {
        CraftPlayer craftPlayer = this.craftPlayer;
        Channel channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channel.pipeline().replace("decoder", "decoder", new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
                if (byteBuf.readableBytes() != 0) {
                    PacketDataSerializer packetDataSerializer = new PacketDataSerializer(byteBuf);
                    int i = packetDataSerializer.e();
                    Packet<?> packet = channelHandlerContext.channel().attr(NetworkManager.c).get().a(EnumProtocolDirection.SERVERBOUND, i);
                    if (packet == null) {
                        throw new IOException("Bad packet id " + i);
                    } else {
                        packet.a(packetDataSerializer);
                        if (packetDataSerializer.readableBytes() > 0) {
                            throw new IOException("Packet " + channelHandlerContext.channel().attr(NetworkManager.c).get().a() + "/" + i + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + packetDataSerializer.readableBytes() + " bytes extra whilst reading packet " + i);
                        } else {
                            if(isDetected(packet, channelHandlerContext) || byteBuf.capacity() > 10000)
                                punish(craftPlayer, channelHandlerContext, packet);
                            else {
                                list.add(packet);
                            }
                        }
                    }
                }
            }
        });
    }

    /*
    isDetected - Sorts through the parties and starts other other checks.
    This method has been made by me.
     */

    private boolean isDetected(Packet<?> packet, ChannelHandlerContext channelHandlerContext) throws NullPointerException {
        if(packet instanceof PacketPlayInBlockPlace) {
            PacketPlayInBlockPlace packetPlayInBlockPlace = (PacketPlayInBlockPlace) packet;
            if(packetPlayInBlockPlace.getItemStack() != null && packetPlayInBlockPlace.getItemStack().getTag() != null)
                if(isInvalidNBT(packetPlayInBlockPlace.getItemStack().getTag())) {
                    punish(craftPlayer, channelHandlerContext, packet);
                    return true;
                }
        }
        if(packet instanceof PacketPlayInSetCreativeSlot) {
            PacketPlayInSetCreativeSlot packetPlayInSetCreativeSlot = (PacketPlayInSetCreativeSlot) packet;
            if(packetPlayInSetCreativeSlot.getItemStack() != null && packetPlayInSetCreativeSlot.getItemStack().getTag() != null)
                if(isInvalidNBT(packetPlayInSetCreativeSlot.getItemStack().getTag())) {
                    punish(craftPlayer, channelHandlerContext, packet);
                    return true;
                }
        }
        if(packet instanceof PacketPlayInWindowClick) {
            PacketPlayInWindowClick packetPlayInWindowClick = (PacketPlayInWindowClick) packet;
            if(packetPlayInWindowClick.e() != null && packetPlayInWindowClick.e().getTag() != null)
                if(isInvalidNBT(packetPlayInWindowClick.e().getTag())) {
                    punish(craftPlayer, channelHandlerContext, packet);
                    return true;
                }
        }
        if(packet instanceof PacketPlayInCustomPayload) {
            PacketPlayInCustomPayload packetPlayInCustomPayload = (PacketPlayInCustomPayload) packet;
            if ("MC|BSign".equals(packetPlayInCustomPayload.a()) || "MC|BEdit".equals(packetPlayInCustomPayload.a())) {
                if(isTrash(craftPlayer, "BOOK")) {
                    punish(craftPlayer, channelHandlerContext, packet);
                    return true;
                }
            }
        }
        return false;
    }

    /*
    isTrash - Simple check for player, checks if the player is holding a 'BOOK' or a 'FIREWORK'.
    This method has been made by me.
     */

    private boolean isTrash(CraftPlayer craftPlayer, String string) {
        if(string.equals("BOOK"))
            if (craftPlayer.getInventory().getItemInHand().getType() != Material.BOOK_AND_QUILL && craftPlayer.getInventory().getItemInHand().getType() != Material.WRITTEN_BOOK)
                return true;
        if(string.equals("FIREWORK"))
            return craftPlayer.getInventory().getItemInHand().getType() != Material.FIREWORK_CHARGE && craftPlayer.getInventory().getItemInHand().getType() != Material.FIREWORK;
        return false;
    }

    /*
    isInvalidNBT - Checks the NBTTagCompound for flags dupes and so on.
    This method has been made by me.
     */

    int count;
    private boolean isInvalidNBT(NBTTagCompound nbtTagCompound) {
        if(nbtTagCompound != null)
            return false;
        assert false;
        if(nbtTagCompound.hasKey("Fireworks") || nbtTagCompound.hasKey("Explosion")) {
            if(nbtTagCompound.toString().length() > 2000)
                return true;
            if(isTrash(craftPlayer, "FIREWORK"))
                return true;
        }
        if(nbtTagCompound.hasKey("pages")) {
            if(nbtTagCompound.getList("pages", 8).size() > 100)
                return true;
            if(isTrash(craftPlayer, "BOOK"))
                return true;
            for (int i = 0; i < nbtTagCompound.getList("pages", 8).size(); i++) {
                count += nbtTagCompound.getList("pages", 8).getString(i).length();
                String content = nbtTagCompound.getList("pages", 8).getString(i);
                if(count > 1000)
                    return true;
                if(!isReadable(content))
                    return true;
                if(content.contains(": {"))
                    return true;
                if(getCount(content, "§".charAt(0)) > 20)
                    return true;
                if(getCount(content, ".".charAt(0)) > 20)
                    return true;
            }
        }
        return false;
    }

    /*
    getCount - Count specific chars in a string.
    This int has been made by me.
     */

    private int getCount(String input, char c) {
        int count = 0;
        for (char act : input.toCharArray()) {
            if (act == c) {
                count++;
            }
        }
        return count;
    }

    /*
    isReadable - Check for common letters.
    This boolean has been found on Google.
     */

    private boolean isReadable(String text) {
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z')
                return true;
            if (c >= 'A' && c <= 'Z')
                return true;
            if (c == 'ö' || c == 'ß' || c == 'ä' || c == 'ü')
                return true;
            if (c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9'
                    || c == '0')
                return true;
            if (c == '!' || c == '"' || c == '$' || c == '%' || c == '&' || c == '/' || c == '(' || c == ')' || c == '{'
                    || c == '}' || c == '[' || c == ']' || c == '=' || c == '?' || c == '@' || c == '*' || c == '+'
                    || c == '~' || c == '<' || c == '>' || c == '|' || c == ';' || c == ',' || c == ':' || c == '-'
                    || c == '_' || c == '.')
                return true;
        }
        return false;
    }

    /*
    format - Some shit required for tps.
    This String has been found on Spigot decompiled.
     */

    private String format(double tps) {
        return ((tps > 19.8) ? "§2§l" : (tps > 18.0) ? "§a§l" : (tps > 16.0) ? "§6§l" : "§4§l")
                + ((tps > 20.0) ? "" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
    }


    /*
    getTPS - Some shit required for tps.
    This StringBuilder has been found on Spigot decompiled.
     */

    private StringBuilder getTPS() {
        StringBuilder sb = new StringBuilder();
        for (double tps : MinecraftServer.getServer().recentTps) {
            sb.append(format(tps));
            sb.append("§8, ");
        }
        return sb;
    }

    /*
    punish - If player has been detected as a moron hes getting kicked and the staff members are getting notified.
    This method has been made by me.
     */

    private void punish(CraftPlayer craftPlayer, ChannelHandlerContext channelHandlerContext, Packet<?> packet) {
        channelHandlerContext.flush();
        channelHandlerContext.close();
        Bukkit.broadcast(
                AntiCrash.getAntiCrash().getPrefix() + "Spieler§8: §e" + craftPlayer.getName()
                + " §8┃ §7Packet§8: §e" + packet.getClass().getSimpleName()
                + " §8┃ §7TPS§8: §e" + getTPS().substring(0, getTPS().length() - 2),
                AntiCrash.getAntiCrash().getPermissions() + "notify");
        Bukkit.getConsoleSender().sendMessage(
                AntiCrash.getAntiCrash().getPrefix() + "Spieler§8: §e" + craftPlayer.getName()
                + " §8┃ §7Packet§8: §e" + packet.getClass().getSimpleName()
                + " §8┃ §7TPS§8: §e" + getTPS().substring(0, getTPS().length() - 2));
    }



}
