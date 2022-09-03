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

public class DeleteNpc implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.delete")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
        }
        int size = args.length;
        if (size < 1){
            sender.sendMessage(ChatColor.RED + "You must input the id of the npc you want to delete");
            return false;
        }
        String id = args[0];
        NpcEntity npc = fromId(id);
        if (npc == null){
            sender.sendMessage(ChatColor.RED + "Inputted id isn't a real npc id!");
            return false;
        }
        npc.remove();
        Npc plugin = Npc.getPlugin(Npc.class);
        plugin.npcFile.getConfig().set("npcs." + id, null);
        plugin.npcFile.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Removed Npc!");
        return true;
    }

    private NpcEntity fromId(String id){
        for (int i = NpcEntity.npcs.size()-1; i >= 0; i--){
            NpcEntity npc = NpcEntity.npcs.get(i);
            if (npc.getId().equalsIgnoreCase(id)) return npc;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 1){
            return npcIds(args[0]);
        }
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
        for (NpcEntity npcEntity : NpcEntity.npcs){
            ids.add(npcEntity.getId());
        }
        return filter(ids, match);
    }
}
