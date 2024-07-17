package me.stephenminer.npc.events;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.packets.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Joining implements Listener {
    private final Npc plugin;
    public Joining(){
        this.plugin = JavaPlugin.getPlugin(Npc.class);
    }

    @EventHandler
    public void addPacketReader(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PacketReader reader = plugin.packetReaderImpl();
        reader.inject(player);
    }

    @EventHandler
    public void removePacketReader(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (NpcEntity npc : NpcEntity.npcs){
            npc.hide(player);
        }
        PacketReader reader = plugin.packetReaderImpl();
        reader.unInject(player);
    }

    @EventHandler
    public void showNpcs(PlayerJoinEvent event){
        Player player = event.getPlayer();
        World world = player.getWorld();
        for (NpcEntity npc : NpcEntity.npcs){
            if (world.equals(npc.getSpawn().getWorld())) npc.show(player);
        }
    }

    @EventHandler
    public void worldChangeEvent(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        World from = event.getFrom();
        World to = player.getWorld();
        for (NpcEntity npc : NpcEntity.npcs){
            World npcWorld = npc.getSpawn().getWorld();
            if (from.equals(npcWorld)){
                npc.hide(player);
            } else if (to.equals(npcWorld)) {
                npc.show(player);
            }
        }
    }

}
