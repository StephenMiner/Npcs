package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleHalloween implements CommandExecutor {
    private final Npc plugin;
    public ToggleHalloween(Npc plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("npc.commands.halloween")) return false;
        }
        plugin.halloween = !plugin.halloween;
        sender.sendMessage(ChatColor.GOLD + "Halloween set to " + plugin.halloween);
        for (NpcEntity npc : NpcEntity.npcs){
            npc.reloadNpc();
        }
        return true;
    }
}
