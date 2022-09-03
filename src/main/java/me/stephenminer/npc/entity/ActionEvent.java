package me.stephenminer.npc.entity;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionEvent {
    private List<String> dispatch;
    private List<String> send;
    private final ServerPlayer sender;

    public ActionEvent(ServerPlayer npc){
        dispatch = new ArrayList<>();
        send = new ArrayList<>();
        this.sender = npc;
    }

    public void setDispatch(List<String> dispatch){
        this.dispatch = dispatch;
    }

    public void setSend(List<String> send){
        this.send = send;
    }

    public void removeSend(String msg){
        send.remove(msg);
    }
    public void removeDistpatch(String cmd){
        dispatch.remove(cmd);
    }
    public void addSend(String msg){
        send.add(msg);
    }
    public void addDispatch(String cmd){
        dispatch.add(cmd);
    }

    public void dispatchCommands(Player player){
        for (String cmd : dispatch){
            String temp = cmd.replace("[player]", player.getName());
            Bukkit.dispatchCommand(player, temp);
        }
    }

    public void sendMessages(Player player){
        String base = "";
        if (sender != null) {
           base = "<" + sender.getName().getString() + "> ";
        }
        for (String msg : send){
            String temp = msg.replace("[player]", player.getName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', base + temp));
        }
    }

    public List<String> getSend(){
        return send;
    }

    public List<String> getDispatch(){
        return dispatch;
    }



}
