package me.stephenminer.npc.events;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NpcListeners implements Listener {
    private final Npc plugin;
    private final List<UUID> cooldowns;
    public NpcListeners(Npc plugin){
        this.plugin = plugin;
        cooldowns = new ArrayList<>();
    }
    @EventHandler
    public void onNpcClick(NpcInteractEvent event){
        Player player = event.getPlayer();
        if (cooldowns.contains(player.getUniqueId())) return;
        cooldowns.add(player.getUniqueId());
        startClock(player, 1);
        for (NpcEntity entity : NpcEntity.npcs){
            if (entity.npcId() == event.getNpc().npcId()){
                if (event.getAction() == Action.LEFT_CLICK) entity.doOnLeftClick(player);
                else if (event.getAction() == Action.RIGHT_CLICK) entity.doOnRightClick(player);
                return;
            }
        }
     //   player.sendMessage("<" + event.getNpc().getName().getString() + "> Hello " + player.getName() + "!");
    }

    @EventHandler
    public void cleanMap(PlayerQuitEvent event){
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    private void startClock(Player player, int cooldown){
        new BukkitRunnable(){
            private final int ticksInSecond = 20;
            private final int max = cooldown * ticksInSecond;
            private int count = 0;
            @Override
            public void run(){
                if (count >= max){
                    cooldowns.remove(player.getUniqueId());
                    this.cancel();
                }
                count++;
            }

        }.runTaskTimer(plugin, 0, 1);
    }
}
