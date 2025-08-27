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

    public double distSqr(double x, double y, double z){
        return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) + Math.pow(this.z - z, 2);
    }

    public Node cloneMove(int x, int y, int z){
        Node node = new Node(x,y,z);
        node.parent = this.parent;
        node.actualCost = this.actualCost;
        node.estCost = this.estCost;
        node.closed = this.closed;
        return node;
    }

    public boolean posEquals(Node node){
        if (node == null) return false;
        return this.x == node.x && this.y == node.y && this.z == node.z;
    }
}
