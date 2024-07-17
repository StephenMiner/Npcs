package me.stephenminer.v1_20_R3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.events.Action;
import me.stephenminer.npc.events.NpcInteractEvent;
import me.stephenminer.npc.packets.PacketReader;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PacketReaderImpl implements PacketReader {
    private Channel channel;
    @Override
    public void inject(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        channel = getChannel(craftPlayer.getHandle().connection);
        //n
        channels.put(player.getUniqueId(), channel);
        if (channel.pipeline().get("PacketInjector") != null) return;
        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, Packet<?> packet, List<Object> out) throws Exception {
                out.add(packet);
                readPacket(player, packet);
            }
        });
    }

    private Channel getChannel(ServerGamePacketListenerImpl packetListener){
        try{
            Field field = ServerGamePacketListenerImpl.class.getSuperclass().getDeclaredField("c");
            field.setAccessible(true);
            Connection connection = (Connection) field.get(packetListener);
            field.setAccessible(false);
            return connection.channel;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Something wrong happening");
        }
        return null;
    }

    @Override
    public void unInject(Player player)
    {
        channel = (Channel) channels.get(player.getUniqueId());
        if (channel.pipeline().get("PacketInjector") == null) return;
        channel.pipeline().remove("PacketInjector");
    }

    /*
ServerboundInteractPacket:
field a = entity id
field b = action interface
method a() of field b = Type of action (the enum)
 */
    public void readPacket(Player player, Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase(ServerboundInteractPacket.class.getSimpleName())) {
            try {
                int id = (int) getValue(packet, "a");
                Action action;
                Object obj = getValue(packet, "b");
                Method method = obj.getClass().getDeclaredMethod("a");
                method.setAccessible(true);
                action = fromString(method.invoke(obj).toString());
                method.setAccessible(false);
                NpcEntity entity = null;
                for (NpcEntity npc : NpcEntity.npcs) {
                    if (npc.npcId() == id) {
                        entity = npc;
                        break;
                    }
                }
                final NpcEntity npc = entity;
                if (action != null && entity != null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Npc.getPlugin(Npc.class), new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.getServer().getPluginManager().callEvent(new NpcInteractEvent(player, npc, action));
                        }
                    }, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Object getValue(Object instance, String name)
    {
        Object result = null;
        try{
            if (instance instanceof ServerboundInteractPacket packet){
                Field field = packet.getClass().getDeclaredField(name);
                field.setAccessible(true);
                result = field.get(instance);
                field.setAccessible(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
