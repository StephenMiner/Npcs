package me.stephenminer.v1_20_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import me.stephenminer.npc.Npc;
import me.stephenminer.npc.entity.ActionEvent;
import me.stephenminer.npc.entity.NpcEntity;
import me.stephenminer.npc.entity.NpcLoader;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class NpcEntityImpl extends ServerPlayer implements NpcEntity{
    private final Npc plugin;
    private ActionEvent onRightClick;
    private ActionEvent onLeftClick;
    private Location loc;
    private String name;
    private final String id;
    private String skinName;

    public NpcEntityImpl(Location loc, String id, String name){
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld)loc.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(),ChatColor.translateAlternateColorCodes('&',name)));
        this.plugin = JavaPlugin.getPlugin(Npc.class);
        this.loc = loc;
        this.name = name;
        this.id = id;
        this.setPos(loc.getBlockX() + 0.5, loc.getY(), loc.getBlockZ() + 0.5);
        this.setYRot(loc.getYaw());
        this.setYHeadRot(loc.getYaw());
        NpcEntity.npcs.add(this);
        onLeftClick = new ActionEvent(this);
        onRightClick = new ActionEvent(this);
    }

    public void createEntity(){

        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&',name));
       // ClientInformation info = ClientInformation.createDefault();//new ClientInformation("English",1, ChatVisiblity.HIDDEN, true,1,HumanoidArm.RIGHT,false,false);
       // npc = new ServerPlayer(minecraftServer, world, profile);
        updateSkin();
      //  npc.setPos(loc.getBlockX() + 0.5, loc.getY(), loc.getBlockZ() + 0.5);
     //   npc.setYRot(loc.getYaw());
      //  npc.setYHeadRot(loc.getYaw());
       // Connection connection = new FakeConnection(PacketFlow.CLIENTBOUND);
       // npc.connection = new FakePacketListener(minecraftServer,connection,npc);
        onLeftClick = new ActionEvent(this);
        onRightClick = new ActionEvent(this);
       // System.out.println(npc.connection);

    }


    public void updateSkin(){
        if (skinName != null) {
            String[] skin = getSkin(skinName);
            //System.out.println(skinName);
            if (skin != null) {
                this.getGameProfile().getProperties().removeAll("textures");
                this.getGameProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
                this.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
            }
        }
    }



    public void hide(Player player){
        ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        //   connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
        connection.send(new ClientboundRemoveEntitiesPacket(this.getId()));
    }



    public void show(Player player){
        ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
        if (connection == null){
            System.out.println("NULL");
            return;
        }
        connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,this));
     //    connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
        connection.send(new ClientboundAddPlayerPacket(this));
        connection.send(new ClientboundRotateHeadPacket(this, (byte) (this.getYHeadRot()*256 / 360)));
        connection.send(new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData().getNonDefaultValues()));
        if (plugin.halloween){
            List<Pair<EquipmentSlot, ItemStack>> equip = new ArrayList<>();
            equip.add(new Pair<>(EquipmentSlot.HEAD, santahat()));
            connection.send(new ClientboundSetEquipmentPacket(this.getId(),equip));
        }
        ServerPlayer npc = this;
         Bukkit.getScheduler().scheduleSyncDelayedTask(Npc.getPlugin(Npc.class), () -> connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npc.getUUID()))), 50);


    }

    public void remove(){
        List<Player> players = loc.getWorld().getPlayers();
        List<UUID> uuids = new ArrayList<>();
        uuids.add(this.getUUID());
        for (Player player : players){
            ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;

            connection.send(new ClientboundPlayerInfoRemovePacket(uuids));
            connection.send(new ClientboundRemoveEntitiesPacket(this.getId()));
        }
        this.remove(Entity.RemovalReason.DISCARDED);
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

    public boolean isServerPlayer(Object serverPlayer){
        if (serverPlayer instanceof ServerPlayer sp)
            return this.equals(sp);
        else return false;
    }


    public Location getSpawn() {
        return loc;
    }
    public String name(){
        return name;
    }
    public String id(){
        return id;
    }

    public int npcId(){ return this.getId();}

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
    public void teleport(Location target){
        this.setPos(target.getX(), target.getY(), target.getZ());
        this.setYRot(target.getYaw());
        this.setYHeadRot(target.getYaw());
        this.loc = target;
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



    private ItemStack pumpkin(){
        return CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.CARVED_PUMPKIN));
    }

    private ItemStack santahat(){
        String url = "http://textures.minecraft.net/texture/9d0e884a944cc35c1b6dcd0834288156419aeec2c5a157577fc9a7a1ec65c2cf";
        return CraftItemStack.asNMSCopy(skull(url));
    }
    private org.bukkit.inventory.ItemStack skull(String url){
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.PLAYER_HEAD);
        if(url.isEmpty())return item;
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Test");
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try
        {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        item.setItemMeta(meta);
        return item;
    }
}
