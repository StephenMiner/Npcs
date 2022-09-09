package me.stephenminer.npc.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.stephenminer.npc.Npc;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcEntity {
    public static List<NpcEntity> npcs = new ArrayList<>();

    private ActionEvent onRightClick;
    private ActionEvent onLeftClick;
    private final Location loc;
    private String name;
    private final String id;
    private String skinName;
    private ServerPlayer npc;

    public NpcEntity(Location loc, String id, String name){
        this.loc = loc;
        this.name = name;
        this.id = id;
        npcs.add(this);
    }

    public void createEntity(){
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&',name));
        npc = new ServerPlayer(minecraftServer, world, profile, null);
        updateSkin();
        npc.setPos(loc.getBlockX() + 0.5, loc.getY(), loc.getBlockZ() + 0.5);
        npc.setYRot(loc.getYaw());
        npc.setYHeadRot(loc.getYaw());
        onLeftClick = new ActionEvent(npc);
        onRightClick = new ActionEvent(npc);

    }

    public void updateSkin(){
        if (skinName != null) {
            String[] skin = getSkin(skinName);
            System.out.println(skinName);
            if (skin != null) {
                npc.getGameProfile().getProperties().removeAll("textures");
                npc.getGameProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
                npc.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
            }
        }
    }

    public void reloadNpc(){
        List<Player> players = loc.getWorld().getPlayers();
        remove();
        NpcLoader loader = new NpcLoader(Npc.getPlugin(Npc.class), id);
        NpcEntity npcEntity = loader.loadNpc();
        for (Player player : players){
            npcEntity.show(player);
        }
    }

    public void hide(Player player){
        ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
        connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
    }


    private String[] getSkin(String skinId){
        Npc plugin = Npc.getPlugin(Npc.class);
        if (plugin.skinFile.getConfig().contains("skins." + skinId)){
            String[] skin = new String[2];
            skin[0] = plugin.skinFile.getConfig().getString("skins." + skinId + ".skin.textures");
            skin[1] = plugin.skinFile.getConfig().getString("skins." + skinId + ".skin.signature");
            return skin;
        }
        return null;
    }

    public void show(Player player){
        ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(npc));
        connection.send(new ClientboundRotateHeadPacket(npc, (byte) (npc.getYHeadRot()*256 / 360)));
        connection.send(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData(), true));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Npc.getPlugin(Npc.class), () -> connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc)), 50);


    }

    public void remove(){
        List<Player> players = loc.getWorld().getPlayers();
        for (Player player : players){
            ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
            connection.send(new ClientboundRemoveEntitiesPacket(npc.getId()));
        }
        npc.remove(Entity.RemovalReason.DISCARDED);
        npcs.remove(this);
    }

    public void save(){
        Npc plugin = Npc.getPlugin(Npc.class);
        String base = "npcs." + id;
        plugin.npcFile.getConfig().set(base + ".name", name);
        plugin.npcFile.getConfig().set(base+ ".spawn", plugin.fromLoc(loc));
        plugin.npcFile.getConfig().set(base + ".on-left-click.msgs", onLeftClick.getSend());
        plugin.npcFile.getConfig().set(base + ".on-left-click.cmds", onLeftClick.getDispatch());
        plugin.npcFile.getConfig().set(base + ".on-right-click.msgs", onRightClick.getSend());
        plugin.npcFile.getConfig().set(base + ".on-right-click.cmds", onRightClick.getDispatch());
        if (skinName != null){
            plugin.npcFile.getConfig().set(base + ".skin", skinName);
        }
        plugin.npcFile.saveConfig();

    }

    public boolean isServerPlayer(ServerPlayer serverPlayer){
        return npc.equals(serverPlayer);
    }

    public ServerPlayer getNpc(){
        return npc;
    }

    public Location getSpawn() {
        return loc;
    }
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }

    public ActionEvent getOnRightClick(){
        return onRightClick;
    }
    public void setOnRightClick(ActionEvent event){
        onRightClick = event;
    }
    public ActionEvent getOnLeftClick(){
        return  onLeftClick;
    }
    public void setOnLeftClick(ActionEvent event){
        onLeftClick = event;
    }

    public void doOnLeftClick(Player player){
        onLeftClick.dispatchCommands(player);
        onLeftClick.sendMessages(player);
    }
    public void doOnRightClick(Player player){
        onRightClick.dispatchCommands(player);
        onRightClick.sendMessages(player);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSkinName(String name){
        this.skinName = name;
    }
}
