package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreateNpc implements CommandExecutor, TabCompleter {
    private final Npc plugin;

    public CreateNpc(Npc plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.create")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size < 2){
                player.sendMessage(ChatColor.RED + "You need to put an id for your npc and then its name!");
                return false;
            }
            String id = args[0];
            String name;
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < size; i++){
                builder.append(args[i]).append(" ");
            }
            if (builder.length() > 0)
                builder.replace(builder.length() - 1, builder.length(), "");
            name = builder.toString();
            NpcEntity entity = new NpcEntity(player.getLocation(), id, name);
            entity.createEntity();
            World world = player.getWorld();
            for (Player p : world.getPlayers()){
                entity.show(p);
            }
            entity.save();
            player.sendMessage(ChatColor.GREEN + "Created NPC");
        }else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return id();
        else return name();
    }




    private List<String> id(){
        List<String> id = new ArrayList<>();
        id.add("[npc-id]");
        return id;
    }

    private List<String> name(){
        List<String> display = new ArrayList<>();
        display.add("[display-name]");
        return display;
    }

}
