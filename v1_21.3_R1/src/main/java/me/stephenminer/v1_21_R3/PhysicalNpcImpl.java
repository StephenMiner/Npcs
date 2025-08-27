package me.stephenminer.v1_21_R3;

import me.stephenminer.npc.entity.PhysicalNpc;
import me.stephenminer.v1_21_R3.pathfinder.Navigation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R2.event.CraftEventFactory;
import org.bukkit.event.entity.EntityKnockbackEvent;

public class PhysicalNpcImpl extends NpcEntityImpl implements PhysicalNpc {
    private final Navigation navigation;

    public PhysicalNpcImpl(Location loc, String id, String name) {
        super(loc, id, name);
        this.navigation = new Navigation(this, this.level());
        //this.level().addFreshEntity(this);
        //System.out.println(92439545);
    }

    @Override
    public void spawn(World world){
        ((CraftWorld) world).getHandle().addNewPlayer(this);
    }

    @Override
    public void move(Location loc) {
        this.navigation.moveTo(loc.getX(),loc.getBlockY(),loc.getZ(),10,5);
       // this.moveTowardsClosestSpace(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double maxHealth(){
        return this.getAttribute(Attributes.MAX_HEALTH).getValue();
    }

    @Override
    public void setMaxHealth(double health){
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
    }

    @Override
    public double health(){
        return this.getHealth();
    }

    @Override
    public void setHealth(double health){
        super.setHealth((float) health);
    }

    @Override
    public void tick(){

        if (this.connection instanceof FakePacketListener listener){ //should always be true btw...
            /*
            We need to do this here because otherwise our npc getting attacked results in no movement.
            Any movement set before tick() is called seems to get cleared.
             */
            Vec3 deltaMovement = listener.deltaMovement();
            if (!deltaMovement.equals(Vec3.ZERO)){
                this.setDeltaMovement(deltaMovement);
                this.hasImpulse = true;
                listener.setDeltaMovement(Vec3.ZERO);
            }
        }
        super.tick();

        this.aiStep();
    }

    /**
     * Method taken from the Mob class from Mojang
     * @param speed
     */
    @Override
    public void setSpeed(float speed){
        super.setSpeed(speed);
        this.zza = speed;
    }



    @Override
    public boolean isDead(){ return this.dead; }

    @Override
    public int[] pos(){
        return new int[]{ this.getBlockX(), this.getBlockY(), this.getBlockZ() };
    }


    @Override
    public void serverAiStep(){
        super.serverAiStep();
        this.navigation.tick();
        this.navigation.moveControl().tick();
       // System.out.println("TICKING AI STEP");
    }


    @Override
    public void knockback(double d0, double d1, double d2, Entity attacker, EntityKnockbackEvent.KnockbackCause cause) {
        System.out.println("555");
        d0 *= 1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

        Vec3 vec3d;
        for(vec3d = this.getDeltaMovement(); d1 * d1 + d2 * d2 < 9.999999747378752E-6; d2 = (Math.random() - Math.random()) * 0.01) {
            d1 = (Math.random() - Math.random()) * 0.01;
        }

        Vec3 vec3d1 = (new Vec3(d1, 0.0, d2)).normalize().scale(d0);
        EntityKnockbackEvent event = CraftEventFactory.callEntityKnockbackEvent((CraftLivingEntity)this.getBukkitEntity(), attacker, cause, d0, vec3d1, vec3d.x / 2.0 - vec3d1.x, this.onGround() ? Math.min(0.4, vec3d.y / 2.0 + d0) : vec3d.y, vec3d.z / 2.0 - vec3d1.z);
        if (!event.isCancelled()) {
            this.hasImpulse = true;
            this.setDeltaMovement(event.getFinalKnockback().getX(), event.getFinalKnockback().getY(), event.getFinalKnockback().getZ());
            this.move(MoverType.SELF, this.getDeltaMovement());
            System.out.println(2);
        }
        System.out.println(1);

    }


    @Override
    public void knockback(double d0, double d1, double d2){
        System.out.println("444");
        this.knockback(d0,d1,d2,null, EntityKnockbackEvent.KnockbackCause.UNKNOWN);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount);
    }
}
