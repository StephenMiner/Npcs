package me.stephenminer.v1_21_R3.pathfinder;

import me.stephenminer.npc.util.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Taken from Mojang's Path class
 */
public class Path {
    private final List<Node> nodes;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> nodes, BlockPos target, boolean reached){
        this.nodes = nodes;
        this.target = target;
        this.distToTarget = nodes.isEmpty() ? Float.MAX_VALUE : (float) (nodes.get(nodes.size() - 1).distManhattan(target.getX(), target.getY(), target.getZ()));
        this.reached = reached;
    }

    public void advance(){
        ++nextNodeIndex;
    }

    public boolean notStarted(){ return this.nextNodeIndex <= 0; }

    public boolean isDone(){ return this.nextNodeIndex >= nodes.size(); }

    public Node endNode(){
        return !nodes.isEmpty() ? nodes.get(nodes.size() - 1) : null;
    }

    public Node getNode(int index){ return nodes.get(index); }

    public void truncateNodes(int var0) {
        if (this.nodes.size() > var0) {
            this.nodes.subList(var0, this.nodes.size()).clear();
        }

    }

    public void replaceNode(int index, Node node) {
        this.nodes.set(index, node);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int index) {
        this.nextNodeIndex = index;
    }

    public Vec3 getEntityPosAtNode(Entity entity, int index) {
        Node node = this.nodes.get(index);
        double var3 = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
        double var5 = (double)node.y;
        double var7 = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
        return new Vec3(var3, var5, var7);
    }

    public BlockPos getNodePos(int index) {
        Node node = this.nodes.get(index);
        return new BlockPos(node.x, node.y, node.z);
    }

    public Vec3 getNextEntityPos(Entity var0) {
        return this.getEntityPosAtNode(var0, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return getNodePos(nextNodeIndex);
    }

    public Node getNextNode() {
        return this.nodes.get(nextNodeIndex);
    }

    public BlockPos targetPos(){ return target; }

    @Nullable
    public Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(Path path) {
        if (path == null) {
            return false;
        } else if (path.nodes.size() != this.nodes.size()) {
            return false;
        } else {
            for(int i = 0; i < this.nodes.size(); i++) {
                Node node = this.nodes.get(i);
                Node otherNode = path.nodes.get(i);
                if (node.x != otherNode.x || node.y != otherNode.y || node.z != otherNode.z) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean reached(){ return reached; }


}
