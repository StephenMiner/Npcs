package me.stephenminer.npc.packets;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.events.Action;
import me.stephenminer.npc.events.NpcInteractEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PacketReader {
    private Channel channel;
    public static HashMap<UUID, Channel> channels = new HashMap<>();

    public void inject(Player player){
        CraftPlayer craftPlayer = (CraftPlayer) player;
        channel = craftPlayer.getHandle().connection.connection.channel;
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

    public void unInject(Player player){
        channel = channels.get(player.getUniqueId());
        if (channel.pipeline().get("PacketInjector") == null) return;
        channel.pipeline().remove("PacketInjector");
    }

    /*
    ServerboundInteractPacket:
    field a = entity id
    field b = action interface
    method a() of field b = Type of action (the enum)
     */
    public void readPacket(Player player, Packet<?> packet){
        if (packet.getClass().getSimpleName().equalsIgnoreCase(ServerboundInteractPacket.class.getSimpleName())){
            try {
                int id = (int) getValue(packet, "a");
                Action action;
                Object obj = getValue(packet, "b");
                Method method = obj.getClass().getDeclaredMethod("a");
                method.setAccessible(true);
                action = fromString(method.invoke(obj).toString());
                method.setAccessible(false);
                NpcEntity entity = null;
                for (NpcEntity npc : NpcEntity.npcs){
                    if (npc.getNpc().getId() == id) {
                        entity = npc;
                        break;
                    }
                }
                final NpcEntity npc = entity;
                if (action != null && entity != null){
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Npc.getPlugin(Npc.class), new Runnable() {
                        @Override
                        public void run() {
                            Bukkit.getServer().getPluginManager().callEvent(new NpcInteractEvent(player, npc.getNpc(), action));
                        }
                    }, 0);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private Action fromString(String str){
        return switch (str){
            case "INTERACT" -> Action.RIGHT_CLICK;
            case "ATTACK" -> Action.LEFT_CLICK;
            default -> null;
        };
    }

    private Object getValue(Object instance, String name){
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
