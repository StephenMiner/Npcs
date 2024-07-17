package me.stephenminer.npc.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.stephenminer.npc.Npc;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoadSkin implements CommandExecutor, TabCompleter {
    private final Npc plugin;


    public LoadSkin(Npc plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if (sender instanceof Player player){
            if (!player.hasPermission("npcs.commands.loadskin")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size < 2){
                sender.sendMessage(ChatColor.RED + "Please specify the username whose skin you want to load and the id you want to save it as!");
                return false;
            }
            String[] skin = loadSkin(args[1]);
            plugin.skinFile.getConfig().set("skins." + args[0] + ".skin.user", args[1]);
            plugin.skinFile.getConfig().set("skins." + args[0] + ".skin.textures", skin[0]);
            plugin.skinFile.getConfig().set("skins." + args[0] + ".skin.signature", skin[1]);
            plugin.skinFile.saveConfig();
            player.sendMessage(ChatColor.GREEN + "Loaded skin");
            return true;
        } else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return false;
    }


    private String[] loadSkin(String name){
        try{
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();

            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new String[]{texture, signature};
        }catch (Exception e){
            e.printStackTrace();
            plugin.getLogger().warning("Couldn't load skin for inputted username!");
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return skinId();
        if (size == 2) return username();
        return null;
    }

    private List<String> skinId(){
        List<String> out = new ArrayList<>();
        out.add("[save-id]");
        return out;
    }
    private List<String> username(){
        List<String> user = new ArrayList<>();
        user.add("[name-of-player-who-holds-skin]");
        return user;
    }
}
