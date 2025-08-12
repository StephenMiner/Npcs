package me.stephenminer.v1_21_R1;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FakePacketListener extends ServerGamePacketListenerImpl {
    public FakePacketListener(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer, CommonListenerCookie clc) {
        super(minecraftServer, networkManager, entityPlayer, clc);
    }

    @Override
    public void send(Packet<?> packet) {
    }

    @Override
    public void resumeFlushing(){

    }
}
