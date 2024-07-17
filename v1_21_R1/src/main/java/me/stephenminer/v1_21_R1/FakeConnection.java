package me.stephenminer.v1_21_R1;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.SocketAddress;

public class FakeConnection extends Connection {
    public FakeConnection(PacketFlow enumprotocoldirection) {
        super(enumprotocoldirection);
        channel = new FakeChannel(null);
        address = new SocketAddress() {
         //   @java.io.Serial
            private static final long serialVersionUID = 8207338859896320185L;
        };
    }

    @Override
    public boolean isConnected(){ return true; }
    @Override
    public void send(Packet<?> packet, PacketSendListener genericfuturelistener) {
    }

/*
    @Override
    public void setListener(PacketListener pl) {
        //q
        try {
            MethodHandles.lookup().unreflectSetter(getField(Connection.class,"q")).invoke(this, pl);
            MethodHandles.lookup().unreflectSetter(getField(Connection.class,"p")).invoke(this,null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

 */

    public Field getField(Class<?> clazz, String name){
        Field field = null;
        try{
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        return field;
    }
}
