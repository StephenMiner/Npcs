package me.stephenminer.npc.entity;

import org.bukkit.World;

public interface PhysicalNpc extends NpcEntity{

    public void spawn(World world);

    public void move();


    double maxHealth();

    double health();

    void setMaxHealth(double health);

    void setHealth(double health);

    void tick();

    boolean isDead();

    int[] pos();

}
