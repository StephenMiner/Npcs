package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.PhysicalNpc;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * This is purely a command for testing purposes!
 * Used to spawn an NPC and give the sender a wand of command to order it around!
 */
public class SpawnCommandableNpc implements CommandExecutor {
    private final Npc plugin;

    public SpawnCommandableNpc(){
        this.plugin = JavaPlugin.getPlugin(Npc.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.spawncommand")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size < 2) {
                player.sendMessage(ChatColor.RED + "You need to specify the type and name of the npc you wish to spawn!");
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
                sender.sendMessage(ChatColor.RED + "Make sure that the provided NPC ID is a real one!");
                return false;
            }
            entity.createEntity();
            entity.spawn(player.getWorld());
            UUID uuid = UUID.randomUUID();
            entity.bindToUUID(uuid);
            World world = player.getWorld();
            for (Player p : world.getPlayers()){
                entity.show(p);
            }
            plugin.taggedNpcs.put(uuid, entity);
            player.getInventory().addItem(commandWand(uuid));
            player.sendMessage(ChatColor.GREEN + "Spawned in your npc and given you a wand of command!");

            return true;
        }else sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
        return false;
    }


    private ItemStack commandWand(UUID boundTo){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Wand of Command");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(plugin.idKey, PersistentDataType.STRING, "wand-of-command");
        NamespacedKey holder = new NamespacedKey(plugin, "holder");
        container.set(holder, PersistentDataType.STRING, boundTo.toString());
        item.setItemMeta(meta);
        return item;
    }


}
