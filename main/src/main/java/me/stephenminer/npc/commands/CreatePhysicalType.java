package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.NpcFile;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CreatePhysicalType implements CommandExecutor, TabCompleter {
    private final Npc plugin;

    public CreatePhysicalType(){
        this.plugin = JavaPlugin.getPlugin(Npc.class);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size < 1){
            sender.sendMessage(ChatColor.RED + "You need to at least input the id of the physical npc type you wish to make!");
            return false;
        }
        String id = args[0];
        if (typeExists(id)){
            sender.sendMessage(ChatColor.RED + "A npc-type of this id already exists! Please use the /editnpctype instead!");
            return false;
        }

        String skin = null;
        int maxHealth = 20;

        if (size >= 2){
            if (!validSkin(args[1])){
                sender.sendMessage(ChatColor.RED + "The input skin id is invalid!");
                return false;
            }
            skin = args[1];

            if (size >= 3){
                try {
                    maxHealth = Integer.parseInt(args[2]);
                }catch (Exception e){
                    sender.sendMessage(ChatColor.RED + args[2] + " is not a valid number!");
                    return false;
                }
            }
            setData(id, skin, maxHealth);
        }


        return false;
    }

    private boolean typeExists(String typeId){
        return plugin.physTypesFile.getConfig().contains(typeId);
    }

    private boolean validSkin(String skinId){
        return plugin.skinFile.getConfig().contains("skins." + skinId);
    }

    private void setData(String typeId, String skinId, int maxHealth){
        NpcFile file = plugin.physTypesFile;
        file.getConfig().set(typeId + ".skin", skinId);
        file.getConfig().set(typeId + ".max-health", maxHealth);
        file.saveConfig();
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){

        return null;
    }




}
