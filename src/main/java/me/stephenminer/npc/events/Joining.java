package me.stephenminer.npc.events;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.packets.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Joining implements Listener {


    @EventHandler
    public void addPacketReader(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PacketReader reader = new PacketReader();
        reader.inject(player);
    }

    @EventHandler
    public void removePacketReader(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (NpcEntity npc : NpcEntity.npcs){
            npc.hide(player);
        }
        PacketReader reader = new PacketReader();
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
