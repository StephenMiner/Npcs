package me.stephenminer.npc.events;

import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class NpcInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final NpcEntity npc;
    private final Action action;
    private boolean cancelled;
    public NpcInteractEvent(Player player, NpcEntity npc, Action action){
        this.player = player;
        this.action = action;
        this.npc = npc;
    }

    public Player getPlayer(){
        return player;
    }
    public NpcEntity getNpc(){
        return npc;
    }
    public Action getAction(){
        return action;
    }

    @Override
    public boolean isCancelled(){
        return cancelled;
    }
    @Override
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
