package me.stephenminer.v1_21_R3;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class FakePacketListener extends ServerGamePacketListenerImpl {
    private Vec3 deltaMovement;

    public FakePacketListener(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer, CommonListenerCookie clc) {
        super(minecraftServer, networkManager, entityPlayer, clc);
        this.deltaMovement = Vec3.ZERO;
    }


    @Override
    public void resumeFlushing() {
    }

    @Override
    public void send(Packet<?> packet) {
        if (packet instanceof ClientboundSetEntityMotionPacket motionPacket){
            if (motionPacket.getId() != player.getId()) return;
            Vec3 velocity = new Vec3(motionPacket.getXa(), motionPacket.getYa(), motionPacket.getZa());
            if (!player.getDeltaMovement().equals(Vec3.ZERO)) {
                /*
                Need to store the movement instead of setting it directly on the ServerPlayer because it gets cleared
                before ServerPlayer#tick() gets run...
                 */
                this.deltaMovement = velocity;
            }

        }
    }

    public Vec3 deltaMovement(){ return deltaMovement; }

    public void setDeltaMovement(Vec3 deltaMovement){
        this.deltaMovement = deltaMovement;
    }
}
