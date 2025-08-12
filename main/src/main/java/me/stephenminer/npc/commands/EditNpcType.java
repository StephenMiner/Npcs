package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EditNpcType implements CommandExecutor, TabCompleter {
    private final Npc plugin;

    public EditNpcType(){
        this.plugin = JavaPlugin.getPlugin(Npc.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("npc.commands.edittype")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        int size = args.length;
        if (size < 2){
            sender.sendMessage(ChatColor.RED + "You need to at least provide a physical npc-type id and a sub command you wish to use");
            return false;
        }
        String subCmd = args[0].toLowerCase();
        String id = args[1];
        if (!typeExists(id)){
            sender.sendMessage(ChatColor.RED + id + " is not a real physical npc type!");
            return false;
        }
        if (size >= 3) {
            if (id.equals("setskin")) {
                String skin = args[2];
                if (!skinExists(skin)){
                    sender.sendMessage(ChatColor.RED + skin + " isn't a skin you have cached!");
                    return false;
                }
                setSkin(id ,skin);
                sender.sendMessage(ChatColor.GREEN + "Set the skin of this physical npc type to " + skin);
                return true;
            }

            if (id.equals("setmaxhealth")) {
                int health = -1;
                try{
                    health = Integer.parseInt(args[2]);
                }catch (Exception ignored){
                }
                if (health < 1){
                    sender.sendMessage(ChatColor.RED + args[2] + " is not a valid integer!");
                    return false;
                }
                setMaxHealth(id, health);
                sender.sendMessage(ChatColor.GREEN + "Set the max health of this physical npc type to " + health);
                return true;
            }
        }
        
        return false;
    }



    private boolean typeExists(String typeId){
        return plugin.physTypesFile.getConfig().contains(typeId);
    }

    private boolean skinExists(String skinId){
        return plugin.physTypesFile.getConfig().contains("skins." + skinId);
    }

    private void setMaxHealth(String typeId, int health){
        plugin.physTypesFile.getConfig().set(typeId + ".max-health", health);
        plugin.physTypesFile.saveConfig();
    }

    private void setSkin(String typeId, String skinId){
        plugin.physTypesFile.getConfig().set(typeId + ".skin", skinId);
        plugin.physTypesFile.saveConfig();
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return subCmds(args[0]);
        if (size == 2) return typeIds(args[1]);
        if (size == 3){
            if (args[1].equals("setskin")) return skins(args[2]);
        }

        return null;
    }



    private List<String> filter(Collection<String> base, String match){
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry).toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }

    private List<String> subCmds(String match){
        List<String> subs = new ArrayList<>();
        subs.add("setskin");
        subs.add("setmaxhealth");
        return filter(subs, match);
    }

    private List<String> typeIds(String match){
        return filter(plugin.physTypesFile.getConfig().getConfigurationSection("").getKeys(false), match);
    }

    private List<String> skins(String match){
        Set<String> skinIds = plugin.skinFile.getConfig().getConfigurationSection("skins").getKeys(false);
        return filter(skinIds, match);
    }

}
