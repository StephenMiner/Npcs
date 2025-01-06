package me.stephenminer.npc.commands;

import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.ActionEvent;
import me.stephenminer.npc.entity.NpcEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditNpc implements CommandExecutor, TabCompleter {
    private final Npc plugin;
    public EditNpc(Npc plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player) {
            if (!player.hasPermission("npcs.commands.edit")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size < 2){
                player.sendMessage(ChatColor.RED + "Not enough arguments!");
                return false;
            }
            if (!isId(args[0])){
                player.sendMessage(ChatColor.RED + "Inputted id isn't a real NPC. Make sure to use /createNpc to create an npc");
                return false;
            }
            String id = args[0];
            String trigger = args[1];
            NpcEntity npc = fromId(id);
            if (trigger.equalsIgnoreCase("teleport")){
                npc.teleport(player.getLocation());
                npc.reloadNpc();
                npc.save();
                player.sendMessage(ChatColor.GREEN + "Moved NPC");
                return true;
            }

            if (size < 3){
                player.sendMessage(ChatColor.RED + "Not enough arguments");
                return false;
            }


            if (trigger.equalsIgnoreCase("setName")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 2; i < size; i++){
                    builder.append(args[i]).append(" ");
                }
                if (builder.length() > 0)
                    builder.replace(builder.length() - 1, builder.length(), "");

                npc.setName(builder.toString());
                npc.save();
                npc.reloadNpc();
                player.sendMessage(ChatColor.GREEN + "Set npc name!");
                return true;
            }
            StringBuilder builder = new StringBuilder();
            if (size < 4){
                player.sendMessage(ChatColor.RED + "Not enough arguments!");
                return false;
            }
            for (int i = 4; i < size; i++) {
                builder.append(args[i]).append(" ");
            }
            if (builder.length() > 0)
                builder.replace(builder.length() - 1, builder.length(), "");
            String msg = builder.toString();
            String type = args[2];
            String subType = args[3];
            if (type.equalsIgnoreCase("callCmd")){
                if (trigger.equalsIgnoreCase("onRightClick")) {
                    if (subType.equalsIgnoreCase("remove")) {
                        npc.getOnRightClick().removeDistpatch(msg);
                    } else if (subType.equalsIgnoreCase("add")){
                        npc.getOnRightClick().addDispatch(msg);
                    }
                } else if (trigger.equalsIgnoreCase("onLeftClick")) {
                    if (subType.equalsIgnoreCase("remove")) {
                        npc.getOnLeftClick().removeDistpatch(msg);
                    } else if (subType.equalsIgnoreCase("add")){
                        npc.getOnLeftClick().addDispatch(msg);
                    }
                }
            }else if (type.equalsIgnoreCase("sendMsg")) {
                if (trigger.equalsIgnoreCase("onRightClick")) {
                    if (subType.equalsIgnoreCase("remove")) {
                        npc.getOnRightClick().removeSend(msg);
                    } else if (subType.equalsIgnoreCase("add")){
                        npc.getOnRightClick().addSend(msg);
                    }
                }
                else if (trigger.equalsIgnoreCase("onLeftClick")) {
                    if (subType.equalsIgnoreCase("remove")) {
                        npc.getOnLeftClick().removeSend(msg);
                    } else if (subType.equalsIgnoreCase("add")) {
                        npc.getOnLeftClick().addSend(msg);
                    }
                }
            }
            npc.save();
            player.sendMessage(ChatColor.GREEN + "Updated your npc");



        }else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return false;
    }


    private boolean isId(String test){
        for (NpcEntity npc : NpcEntity.npcs){
            if (npc.id().equalsIgnoreCase(test)) return true;
        }
        return false;
    }
    private NpcEntity fromId(String id){
        for (NpcEntity npc : NpcEntity.npcs){
            if (npc.id().equalsIgnoreCase(id)) return npc;
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return npcIds(args[0]);
        if (size == 2) return triggers(args[1]);
        if (size == 3) {
            if (args[1].equalsIgnoreCase("setName")) return setName();
            return types(args[2]);
        }
        if (size == 4 && !args[1].equalsIgnoreCase("setName")) return subTypes(args[3]);
        if (size == 5 && !args[1].equalsIgnoreCase("setName")) {
            boolean left = args[1].equalsIgnoreCase("onLeftClick");
            boolean remove = args[3].equalsIgnoreCase("remove");
            if (remove) {
                if (args[2].equalsIgnoreCase("callCmd")) return currentCmds(fromId(args[0]), left, args[4]);
                if (args[2].equalsIgnoreCase("sendMsg")) return currentMsgs(fromId(args[0]), left, args[4]);
            }
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

    private List<String> setName(){
        List<String> name = new ArrayList<>();
        name.add("[name-here]");
        return name;
    }

    private List<String> npcIds(String match){
        List<String> ids = new ArrayList<>();
        for (NpcEntity npc : NpcEntity.npcs){
            ids.add(npc.id());
        }
        return filter(ids, match);
    }

    private List<String> triggers(String match){
        List<String> triggers = new ArrayList<>();
        triggers.add("onLeftClick");
        triggers.add("onRightClick");
        triggers.add("setName");
        triggers.add("setPosition");
        return filter(triggers, match);
    }

    private List<String> types(String match){
        List<String> types = new ArrayList<>();
        types.add("callCmd");
        types.add("sendMsg");
        return filter(types, match);
    }

    private List<String> subTypes(String match){
        List<String> subTypes = new ArrayList<>();
        subTypes.add("add");
        subTypes.add("remove");
        return subTypes;
    }

    private List<String> currentMsgs(NpcEntity npc, boolean left, String match){
        ActionEvent actionEvent = left ? npc.getOnLeftClick() : npc.getOnRightClick();
        return filter(actionEvent.getSend(), match);
    }

    private List<String> currentCmds(NpcEntity npc, boolean left, String match){
        ActionEvent actionEvent = left ? npc.getOnLeftClick() : npc.getOnRightClick();
        return filter(actionEvent.getDispatch(), match);
    }

}
