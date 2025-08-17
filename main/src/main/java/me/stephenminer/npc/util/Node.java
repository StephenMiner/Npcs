package me.stephenminer.npc.util;

import org.bukkit.block.Block;

public final class Node {
    public int x,y,z;
    public double actualCost, estCost;
    public Node parent;
    public boolean closed;

    public Node(int x, int y, int z){
        reset(x,y,z);
    }

    public void reset(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.actualCost = Double.MAX_VALUE;
        this.estCost = 0;
        this.parent = null;
        this.closed = false;
    }

    public double totalCost(){ return actualCost + estCost; }

    public double distManhattan(int x, int y, int z){
        return Math.abs(this.x - x) + Math.abs(this.y - y) + Math.abs(this.z - z);
    }
}
