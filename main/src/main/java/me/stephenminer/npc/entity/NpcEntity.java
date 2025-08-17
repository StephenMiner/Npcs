package me.stephenminer.npc.entity;

import me.stephenminer.npc.Npc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface NpcEntity {
    public static List<NpcEntity> npcs = new ArrayList<>();


    public void createEntity();

    public void updateSkin();

    default void reloadNpc(){
        Npc plugin = JavaPlugin.getPlugin(Npc.class);
        List<Player> players = getSpawn().getWorld().getPlayers();
        //   remove();
        //  NpcLoader loader = new NpcLoader(Npc.getPlugin(Npc.class), id);
        //NpcEntity npcEntity = loader.loadNpc();

        for (Player player : players){
            this.hide(player);

        }
        Bukkit.getScheduler().runTaskLater(plugin,
                ()->getSpawn().getWorld().getPlayers().forEach(this::show), 10);
    }

    public void hide(Player player);


    default String[] getSkin(String skinId){
        Npc plugin = Npc.getPlugin(Npc.class);
        if (plugin.skinFile.getConfig().contains("skins." + skinId)){
            String[] skin = new String[2];
            skin[0] = plugin.skinFile.getConfig().getString("skins." + skinId + ".skin.textures");
            skin[1] = plugin.skinFile.getConfig().getString("skins." + skinId + ".skin.signature");
            return skin;
        }
        return null;
    }

    /**
     * Show NPC to player via Packets sent to client
     * @param player
     */
    public void show(Player player);

    /**
     * Remove the NPC entirely, sends entity removal packets
     */
    public void remove();

    /**
     * saves this npc's settings to a yaml file npcs.yml
     */
    public void save();


    /**
     *
     * @param serverPlayer MUST BE AN INSTANCE OF ServerPlayer, it is only an Object param for NMS reasons
     * @return true if the passed params = npc's ServerPlayer, false otherwise
     */
    public boolean isServerPlayer(Object serverPlayer);


    public Location getSpawn();
    public String name();
    public String id();
    //The ServerPlayer id
    public int npcId();

    public ActionEvent getOnRightClick();
    public void setOnRightClick(ActionEvent event);
    public ActionEvent getOnLeftClick();
    public void setOnLeftClick(ActionEvent event);

    public void doOnLeftClick(Player player);
    public void doOnRightClick(Player player);

    public void setName(String name);

    public void setSkinName(String name);

    public void teleport(Location target);

    public void setLocation(Location target);

    public Player bukkit();




}
