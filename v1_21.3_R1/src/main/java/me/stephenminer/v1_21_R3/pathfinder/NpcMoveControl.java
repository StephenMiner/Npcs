package me.stephenminer.v1_21_R3.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NpcMoveControl {
    protected static final int MAX_TURN = 90;
    protected final ServerPlayer npc;
    protected double wantedX, wantedY, wantedZ, speedMod;
    protected float strafeForward, strafeRight;
    protected Operation operation;
    protected boolean jump;

    public NpcMoveControl(ServerPlayer npc){
        this.npc = npc;
    }


    public void strafe(float strafeForward, float strafeRight){
        this.operation = Operation.STRAFE;
        this.strafeForward = strafeForward;
        this.strafeRight = strafeRight;
        this.speedMod = 0.25;
    }

    public void tick(){
        float zMotion;
        if (operation == Operation.STRAFE){
            float attributeSpeed = (float) this.npc.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float speed = (float) (attributeSpeed * speedMod);
            float forwards = this.strafeForward;
            float right = this.strafeRight;
            float hypMotion = Mth.sqrt(forwards * forwards + right * right);
            if (hypMotion < 1F) hypMotion = 1F;
            hypMotion = speed / hypMotion;
            forwards *= hypMotion;
            right *= hypMotion;
            float vertRot = Mth.sin(this.npc.getYRot() * 0.017453292F);
            float horRot = Mth.cos(this.npc.getYRot() * 0.017453292F);
            float xMotion = forwards * horRot - right * vertRot;
            zMotion = right * horRot + forwards * vertRot;
            if (!isWalkable(xMotion, zMotion)){
                this.strafeForward = 1.0F;
                this.strafeRight = 0.0F;
            }
            this.npc.setSpeed(speed);
            this.npc.xxa = this.strafeForward;
            this.npc.zza = this.strafeRight;
            this.operation = Operation.WAIT;
        }else if (this.operation == Operation.MOVE_TO){
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.npc.getX();
            double dy = this.wantedY - this.npc.getY();
            double dz = this.wantedZ - this.npc.getZ();
            double distSqr = dx * dx + dy * dy + dz * dz;
            if (distSqr < 2.500000277905201E-7){
                this.npc.zza = 0;
                return;
            }

            zMotion = (float) (Math.atan2(dz, dx) * 57.2957763671875) - 90F;
            this.npc.setYRot(this.rotLerp(this.npc.getYRot(),zMotion, 90F));
            this.npc.setSpeed((float) (this.speedMod * this.npc.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos pos = this.npc.blockPosition();
            Level level = this.npc.level();
            BlockState state = level.getBlockState(pos);
            VoxelShape collision = state.getCollisionShape(level, pos);
            double flatManhattan = dx * dx + dz * dz;
            if ((dy > (double) this.npc.maxUpStep() && flatManhattan < (double) Math.max(1.0f, this.npc.getBbWidth()))
                    || (!collision.isEmpty() && this.npc.getY() < collision.max(Direction.Axis.Y) + pos.getY() && !state.is(BlockTags.FENCES))){
                //this.npc.moveCo
                this.jump = true;
                this.operation = Operation.JUMPING;
            }
        }else if (this.operation == Operation.JUMPING){
            this.npc.setSpeed((float) (this.speedMod * this.npc.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.npc.onGround())
                this.operation = Operation.WAIT;
        }else{
            this.npc.zza = 0.0f;
        }


        this.npc.setJumping(this.jump);
        this.jump = false;
    }

    public boolean hasWanted(){ return operation == Operation.MOVE_TO; }

    public double speedmod(){ return speedMod; }

    public void setWantedPosition(double x, double y, double z, double speedMod){
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.speedMod = speedMod;
        if (this.operation != Operation.JUMPING)
            this.operation = Operation.MOVE_TO;
    }

    private boolean isWalkable(float forwards, float right){
        return isWalkable(BlockPos.containing(this.npc.getX() + forwards, this.npc.getY(), this.npc.getZ() + right));
    }



    private boolean isWalkable( int x, int y, int z){
        BlockPos pos = new BlockPos(x, y, z);
        return isWalkable(pos);
    }
    private boolean isWalkable(BlockPos pos){
        Level level = this.npc.level();
        BlockState foot = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());
        boolean walkable = foot.getCollisionShape(level, pos).isEmpty()
                && head.getCollisionShape(level, pos.above()).isEmpty()
                && !below.getCollisionShape(level, pos.below()).isEmpty();
        return walkable;
    }

    protected float rotLerp(float currentRot, float targetRot, float rotLimit){
        float angleDist = Mth.wrapDegrees(targetRot - currentRot);
        if (angleDist > rotLimit)
            angleDist = rotLimit;
        if (angleDist < -rotLimit)
            angleDist = -rotLimit;

        float rot = currentRot + angleDist;
        if (rot < 0.0)
            rot += 360.0f; //normalize degrees to be in certain range
        else if (rot > 360)
            rot -= 360.0f;
        return rot;
    }



    protected static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;

        private Operation() {
        }
    }
}
