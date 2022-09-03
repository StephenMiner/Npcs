package me.stephenminer.npc.entity;

import me.stephenminer.npc.Npc;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;

import java.util.List;

public class NpcLoader {
    private final String id;
    private final Npc plugin;
    public NpcLoader(Npc plugin, String id){
        this.plugin = plugin;
        this.id = id;
    }

    public ActionEvent loadActionEvent(ServerPlayer npc, boolean left){
        String click = left ? "on-left-click" : "on-right-click";
        String base = "npcs." + id + "." + click;
        List<String> dispatch = plugin.npcFile.getConfig().getStringList(base + ".cmds");
        List<String> send = plugin.npcFile.getConfig().getStringList(base + ".msgs");
        ActionEvent event = new ActionEvent(npc);
        event.setDispatch(dispatch);
        event.setSend(send);
        return event;
    }

    public NpcEntity loadNpc(){
        String base = "npcs." + id;
        String name = plugin.npcFile.getConfig().getString(base + ".name");
        Location loc = plugin.fromString(plugin.npcFile.getConfig().getString(base + ".spawn"));
        try{
            NpcEntity npc =  new NpcEntity(loc, id, name);
            if (plugin.npcFile.getConfig().contains(base + ".skin")){
                npc.setSkinName(plugin.npcFile.getConfig().getString(base + ".skin"));
            }
            npc.createEntity();
            npc.setOnLeftClick(loadActionEvent(npc.getNpc(), true));
            npc.setOnRightClick(loadActionEvent(npc.getNpc(), false));
            return npc;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
