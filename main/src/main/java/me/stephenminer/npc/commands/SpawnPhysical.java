package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.entity.PhysicalNpc;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnPhysical implements CommandExecutor {
    private final Npc plugin;

    public SpawnPhysical(){
        this.plugin = JavaPlugin.getPlugin(Npc.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.spawnphysical")){
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
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
            PhysicalNpc entity = plugin.loadPhysByType(player.getLocation(), id, name);
            if (entity == null){
                sender.sendMessage(ChatColor.RED + "Failed to load Physical NPC " + id);
                return false;
            }
            entity.createEntity();
            entity.spawn(player.getWorld());
            World world = player.getWorld();

            for (Player p : world.getPlayers()){
                entity.show(p);
            }
           // entity.save();

            player.sendMessage(ChatColor.GREEN + "Created NPC");
        }else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return false;
    }
}
