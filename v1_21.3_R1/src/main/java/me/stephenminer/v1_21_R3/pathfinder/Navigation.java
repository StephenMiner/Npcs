package me.stephenminer.v1_21_R3.pathfinder;

import me.stephenminer.npc.util.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;


/**
 * Code from Mojang's PathNavigation class
 */
public class Navigation {
    private static final int MAX_TIME_RECOMPUTE = 20;
    private static final int STUCK_CHECK_INTERVAL = 100;
    private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;

    protected final ServerPlayer npc;
    protected final Level world;

    protected Path path;
    protected double speedMod;
    protected int tick;
    protected int lastStuckCheck;
    protected Vec3 lastStuckPos;
    protected Vec3i timeoutCachedNode;

    protected long timeoutTimer;
    protected long lastTimeoutCheck;
    protected double timeoutLimit;
    protected float maxDistanceToWaypoint;
    protected boolean hasDelayedRecomputation;
    protected long timeLastRecompute;
    private NpcMoveControl moveControl;
    @Nullable
    private BlockPos targetPos;
    private int reachRange;
    private float maxVisitedNodesMultiplier;
    private final PathFinder pathFinder;
    private boolean isStuck;
    private float requiredPathLength;

    public Navigation(ServerPlayer npc, Level level){
        this.npc = npc;
        this.world = level;
        this.lastStuckPos = Vec3.ZERO;
        this.timeoutCachedNode = Vec3i.ZERO;
        this.maxDistanceToWaypoint = 0.5F;
        this.maxVisitedNodesMultiplier = 1.0F;
        this.requiredPathLength = 16.0F;
        this.pathFinder = new PathFinder(2,1);
        this.moveControl = new NpcMoveControl(npc);
    }

    public boolean moveTo(double x, double y, double z, int range, double speedMod){
        Path path = this.createPath(BlockPos.containing(x,y,z), range, false);
        if (path == null){
            this.path = null;
            return false;
        }else{
            if (!path.sameAs(this.path))
                this.path = path;
            if (!this.hasPath()){
                return false;
            }else{
                this.trimPath();
                if (this.path.getNodeCount() <= 0) return false;

                this.speedMod = speedMod;
                return true;
            }
        }
    }

    protected Path createPath(BlockPos pos, int range, boolean startAbove){
        if (pos == null) return null;
        else if (this.npc.getY() < (float) this.world.getMinY()) return null;
        else if (this.path != null && !this.path.isDone() && pos.equals(this.targetPos)) return this.path;
        else{
            BlockPos start = startAbove ? npc.blockPosition().above() : npc.blockPosition();
            Path path = pathFinder.findPath(world, start, pos, range);
            if (path != null){
                this.targetPos = path.targetPos();
                this.reachRange = range;
                this.resetStuckTimer();
            }
            return path;
        }
    }


    public boolean hasPath(){
        return this.path != null && !this.path.isDone();
    }

    public void tick(){
        if (!this.hasPath()) return;
        Vec3 target = this.path.getNextEntityPos(npc);
        Vec3 current = npc.position();

        Vec3 delta = target.subtract(current);
        double distSq = delta.lengthSqr();

        if (distSq < 0.05){
            System.out.println(1);
            path.advance();
            if (path.isDone()) {
                if (!path.reached()){
                    path = pathFinder.findPath(world, npc.blockPosition(), path.targetPos(), 10);
                }else path = null;
                return;
            }
        }
        if (!path.isDone()){
         //   System.out.println(2);
            Vec3 nextPos = this.path.getNextEntityPos(npc);
            moveControl.setWantedPosition(nextPos.x, nextPos.y, nextPos.z, speedMod);
        }
       // Vec3 movement =

    }





    private void resetStuckTimer(){
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0;
        this.isStuck = false;
    }

    protected void trimPath() {
        if (this.path != null) {
            for(int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                Node otherNode = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                BlockState state = this.world.getBlockState(new BlockPos(node.x, node.y, node.z));
                if (state.is(BlockTags.CAULDRONS)) {
                    this.path.replaceNode(i, node.cloneMove(node.x, node.y + 1, node.z));
                    if (otherNode != null && node.y >= otherNode.y) {
                        this.path.replaceNode(i + 1, node.cloneMove(otherNode.x, node.y + 1, otherNode.z));
                    }
                }
            }

        }
    }

    public NpcMoveControl moveControl(){ return moveControl; }

    public PathFinder pathfinder(){ return pathFinder; }


}
