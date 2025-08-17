package me.stephenminer.npc.entity;

import me.stephenminer.npc.Npc;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public interface PhysicalNpc extends NpcEntity{

    public void spawn(World world);

    public void move(Location loc);


    double maxHealth();

    double health();

    void setMaxHealth(double health);

    void setHealth(double health);

    void tick();

    boolean isDead();

    int[] pos();

    default void bindToUUID(UUID uuid){
        Player player = bukkit();
        NamespacedKey tag = new NamespacedKey(JavaPlugin.getPlugin(Npc.class), "command-id");
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(tag, PersistentDataType.STRING, uuid.toString());
    }


}
