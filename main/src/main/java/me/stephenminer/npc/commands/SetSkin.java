package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SetSkin implements CommandExecutor, TabCompleter {
    private final Npc plugin;
    public SetSkin(Npc plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.setskin")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
        }
        int size = args.length;
        if (size < 2){
            sender.sendMessage(ChatColor.RED + "You need to input an npc and the skin-id you want it to have!");
            return false;
        }
        NpcEntity npc = fromId(args[0]);
        if (npc != null){
            if (hasSkin(args[1])){
                //  List<Player> players = npc.getSpawn().getWorld().getPlayers();
                npc.setSkinName(args[1]);
                npc.save();
                npc.updateSkin();
                npc.reloadNpc();
                sender.sendMessage(ChatColor.GREEN + "Set the skin for your npc (you may need to relog to see changes)");
                return true;
            }else sender.sendMessage(ChatColor.RED + "Inputted skin-id is invalid!");
        }else sender.sendMessage(ChatColor.RED + "Inputted npc-id is invalid!");
        return false;
    }

    private NpcEntity fromId(String id){
        for (NpcEntity npc : NpcEntity.npcs){
            if (npc.id().equalsIgnoreCase(id)) return npc;
        }
        return null;
    }
    private boolean hasSkin(String skinId){
        return plugin.skinFile.getConfig().contains("skins." + skinId);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return npcIds(args[0]);
        if (size == 2) return skinIds(args[1]);
        return null;
    }

    private List<String> filter(Collection<String> base, String match){
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = entry.toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }

    private List<String> npcIds(String match){
        List<String> ids = new ArrayList<>();
        for (NpcEntity npc : NpcEntity.npcs){
            ids.add(npc.id());
        }
        return filter(ids, match);
    }
    private List<String> skinIds(String match){
        if (plugin.skinFile.getConfig().contains("skins")){
            Set<String> ids = plugin.skinFile.getConfig().getConfigurationSection("skins").getKeys(false);
            return filter(ids, match);
        }
        return null;
    }
}
