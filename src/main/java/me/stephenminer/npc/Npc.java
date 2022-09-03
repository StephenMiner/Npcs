package me.stephenminer.npc;

import me.stephenminer.npc.commands.*;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.entity.NpcLoader;
import me.stephenminer.npc.events.Joining;
import me.stephenminer.npc.events.NpcListeners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class Npc extends JavaPlugin {

    public NpcFile npcFile;
    public NpcFile skinFile;
    @Override
    public void onEnable() {
        registerCommands();
        registerEvents();
        npcFile = new NpcFile(this, "npcs");
        skinFile = new NpcFile(this, "skins");
        loadNpcs();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        clearEntities();
        // Plugin shutdown logic
    }

    private void registerCommands(){
        DeleteNpc deleteNpc = new DeleteNpc();
        getCommand("deleteNpc").setExecutor(deleteNpc);
        getCommand("deleteNpc").setTabCompleter(deleteNpc);

        CreateNpc createNpc = new CreateNpc(this);
        getCommand("createNpc").setExecutor(createNpc);
        getCommand("createNpc").setTabCompleter(createNpc);

        EditNpc editNpc = new EditNpc(this);
        getCommand("editNpc").setExecutor(editNpc);
        getCommand("editNpc").setTabCompleter(editNpc);

        LoadSkin loadSkin = new LoadSkin(this);
        getCommand("loadSkin").setExecutor(loadSkin);
        getCommand("loadSkin").setTabCompleter(loadSkin);

        SetSkin setSkin = new SetSkin(this);
        getCommand("setSkin").setExecutor(setSkin);
        getCommand("setSkin").setTabCompleter(setSkin);
    }

    private void registerEvents(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Joining(), this);
        pm.registerEvents(new NpcListeners(this), this);
    }

    private void clearEntities(){
        for (int i = NpcEntity.npcs.size()-1; i >= 0; i--){
            NpcEntity npc = NpcEntity.npcs.get(i);
            npc.remove();
        }
    }

    private void loadNpcs(){
        if (npcFile.getConfig().contains("npcs")){
            Set<String> entries = npcFile.getConfig().getConfigurationSection("npcs").getKeys(false);
            for (String id : entries){
                NpcLoader loader = new NpcLoader(this, id);
                loader.loadNpc();
            }
        }
    }

    public String fromLoc(Location loc){
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public Location fromString(String str){
        String[] contents = str.split(",");
        double x = Double.parseDouble(contents[1]);
        double y = Double.parseDouble(contents[2]);
        double z = Double.parseDouble(contents[3]);
        float yaw = Float.parseFloat(contents[4]);
        float pitch = Float.parseFloat(contents[5]);
        String worldName = contents[0];
        try{
            World world = Bukkit.getWorld(worldName);
            if (world == null){
                world = Bukkit.createWorld(new WorldCreator(worldName));
            }
            return new Location(world, x, y, z, yaw, pitch);
        }catch (Exception e){
            getLogger().warning("Couldn't load location " + str + " because the world is null!");
        }
        return null;
    }
}
