package me.stephenminer.npc;

import me.stephenminer.npc.commands.*;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.entity.NpcLoader;
import me.stephenminer.npc.entity.PhysicalNpc;
import me.stephenminer.npc.events.Joining;
import me.stephenminer.npc.events.NpcListeners;
import me.stephenminer.npc.packets.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.C;

import java.util.Set;

public final class Npc extends JavaPlugin {

    public NpcFile npcFile;
    public NpcFile skinFile;
    public NpcFile physTypesFile;
    public boolean halloween = true;
    @Override
    public void onEnable() {
        registerCommands();
        registerEvents();
        npcFile = new NpcFile(this, "npcs");
        skinFile = new NpcFile(this, "skins");
        this.physTypesFile = new NpcFile(this, "physical-npcs");
        loadNpcs();
        //tickNpcs();
        // Plugin startup logic

    }

    /*
    public void tickNpcs(){
        Bukkit.getScheduler().runTaskTimer(this,()->{
            for (NpcEntity npc : NpcEntity.npcs){
                if (npc instanceof PhysicalNpc physicalNpc && !physicalNpc.isDead()) {
                    physicalNpc.tick();
                    int[] pos = physicalNpc.pos();
                    System.out.println(pos[0] + "," + pos[1] + "," + pos[2]);
                }
            }
        },1,1);

    }

     */

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

        getCommand("holidayNpc").setExecutor(new ToggleHalloween(this));

        getCommand("spawnPhysical").setExecutor(new SpawnPhysical());

        CreatePhysicalType createPhysicalType = new CreatePhysicalType();
        getCommand("createphysical").setExecutor(createPhysicalType);
        getCommand("createphysical").setTabCompleter(createPhysicalType);

        EditNpcType editNpcType = new EditNpcType();
        getCommand("editphysical").setExecutor(editNpcType);
        getCommand("editphysical").setTabCompleter(editNpcType);


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

    /**
     * Gets an NpcEntity object using the correct NMS implementation
     * @return
     */

    public NpcEntity npcImpl(Location loc, String id, String displayName){
        NpcEntity npc;
        String packageName = "me.stephenminer";
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
        try {
            switch (ver){
                case "1.21" ->{
                    return (NpcEntity) Class.forName(packageName + ".v1_21_R1.NpcEntityImpl").getConstructor(Location.class, String.class, String.class).newInstance(loc, id, displayName);
                }
                case "1.21.3" ->{
                    return (NpcEntity) Class.forName(packageName + ".v1_21_R3.NpcEntityImpl").getConstructor(Location.class, String.class, String.class).newInstance(loc, id, displayName);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        try{

            String name = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            npc = (NpcEntity) Class.forName(packageName +"." +  name + ".NpcEntityImpl").getConstructor(Location.class, String.class, String.class).newInstance(loc, id, displayName);
            return npc;
        }catch (Exception e){ e.printStackTrace();}
        return null;
    }

    public PhysicalNpc physicalImpl(Location loc, String id, String displayName){
        NpcEntity npc;
        String packageName = "me.stephenminer";
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf('-'));
        try{
            switch (ver){
                case "1.21.3" -> { return (PhysicalNpc) Class.forName(packageName +  ".v1_21_R3.PhysicalNpcImpl").getConstructor(Location.class
                , String.class, String.class).newInstance(loc,id,displayName); }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets an PacketReader object using the correct NMS implementation
     * @return
     */
    public PacketReader  packetReaderImpl(){
        String packageName = "me.stephenminer";
        PacketReader reader;
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
       // System.out.println(ver);
        try {
            switch (ver){
                case "1.21" -> {
                    return (PacketReader) Class.forName(packageName + ".v1_21_R1.PacketReaderImpl").getConstructor().newInstance();
                }
                case "1.21.3" -> {
                    return (PacketReader) Class.forName(packageName + ".v1_21_R3.PacketReaderImpl").getConstructor().newInstance();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            String name = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            reader = (PacketReader) Class.forName(packageName +"." +  name + ".PacketReaderImpl").getConstructor().newInstance();
            return reader;
        }catch (Exception e){ e.printStackTrace();}
        return null;
    }


    public PhysicalNpc loadPhysByType(Location position, String typeId, String name){
        if (!this.physTypesFile.getConfig().contains(typeId)) return null;
        String skinId = this.physTypesFile.getConfig().getString(typeId + ".skin");
        int maxHp = this.physTypesFile.getConfig().getInt(typeId + ".max-health");
        PhysicalNpc npc = this.physicalImpl(position, typeId, name);
        if (npc == null){
            System.err.println("Something went wrong loading the PhysicalNpc object for " + typeId + ". Maybe server version is invalid?");
            return null;
        }
        npc.setSkinName(skinId);
        npc.setMaxHealth(maxHp);
        npc.setHealth(maxHp);
        return npc;
    }
}
