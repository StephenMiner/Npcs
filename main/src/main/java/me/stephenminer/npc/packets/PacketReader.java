package me.stephenminer.npc.packets;

import me.stephenminer.npc.events.Action;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public interface PacketReader {

    public static HashMap<UUID, Object> channels = new HashMap<>();

    void inject(Player player);
    void unInject(Player player);



    default Action fromString(String str){
        return switch (str){
            case "INTERACT" -> Action.RIGHT_CLICK;
            case "ATTACK" -> Action.LEFT_CLICK;
            default -> null;
        };
    }

     Object getValue(Object instance, String name);
}
